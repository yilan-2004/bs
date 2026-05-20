package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.entity.QuestionOption;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.QuestionOptionMapper;
import com.agentedu.service.judge.JudgeResult;
import com.agentedu.service.judge.TestCaseJudgeResult;
import com.agentedu.utils.HashUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChoiceEvaluateService {

    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 单选题评测，学生答案使用选项标识，例如 A/B/C/D。
     */
    public JudgeResult evaluate(Problem problem, String studentAnswer) {
        return evaluate(problem, studentAnswer, false);
    }

    /**
     * 多选题评测，学生答案使用选项标识组合，例如 ACD；比较时忽略大小写、逗号和空格。
     */
    public JudgeResult evaluateMultiChoice(Problem problem, String studentAnswer) {
        return evaluate(problem, studentAnswer, true);
    }

    private JudgeResult evaluate(Problem problem, String studentAnswer, boolean multiChoice) {
        List<QuestionOption> options = questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                .eq(QuestionOption::getProblemId, problem.getId())
                .orderByAsc(QuestionOption::getSortOrder)
                .orderByAsc(QuestionOption::getId));
        if (options.size() < 2) {
            throw new BusinessException("选择题选项配置不完整");
        }

        List<QuestionOption> correctOptions = options.stream()
                .filter(option -> Integer.valueOf(1).equals(option.getIsCorrect()))
                .toList();
        if (correctOptions.isEmpty()) {
            throw new BusinessException("选择题正确选项未配置");
        }
        if (!multiChoice && correctOptions.size() != 1) {
            throw new BusinessException("单选题必须且只能配置一个正确选项");
        }

        String normalizedAnswer = normalize(studentAnswer);
        String correctAnswer = correctOptions.stream()
                .map(QuestionOption::getOptionKey)
                .map(this::normalize)
                .sorted()
                .collect(Collectors.joining());
        boolean accepted = StringUtils.hasText(normalizedAnswer) && normalizedAnswer.equalsIgnoreCase(correctAnswer);
        return buildResult(problem, normalizedAnswer, correctAnswer, accepted, multiChoice);
    }

    private JudgeResult buildResult(Problem problem, String studentAnswer, String correctAnswer,
                                    boolean accepted, boolean multiChoice) {
        JudgeResult result = new JudgeResult();
        result.setJudgeStatus(accepted ? JudgeStatusEnum.ACCEPTED.name() : JudgeStatusEnum.WRONG_ANSWER.name());
        result.setPassCount(accepted ? 1 : 0);
        result.setTotalCount(1);
        result.setRunTime(0L);
        result.setNeedAiFeedback(accepted ? 0 : 1);
        result.setOutputResult(studentAnswer);
        if (!accepted) {
            result.setErrorMessage(multiChoice ? "多选题答案错误" : "选择题答案错误");
            String questionType = multiChoice ? "MULTI_CHOICE" : "CHOICE";
            result.setErrorFingerprint(HashUtils.md5(problem.getId() + questionType + studentAnswer + correctAnswer));
        }

        TestCaseJudgeResult caseResult = new TestCaseJudgeResult();
        caseResult.setTestCaseId(0L);
        caseResult.setInputData("学生答案：" + studentAnswer);
        caseResult.setExpectedOutput(correctAnswer);
        caseResult.setActualOutput(studentAnswer);
        caseResult.setErrorOutput(accepted ? "" : result.getErrorMessage());
        caseResult.setJudgeStatus(result.getJudgeStatus());
        caseResult.setRunTime(0L);
        caseResult.setPassFlag(accepted ? 1 : 0);
        result.getTestCaseResults().add(caseResult);
        return result;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace("，", "")
                .replace(",", "")
                .replace("、", "")
                .replace(" ", "")
                .toUpperCase()
                .chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .distinct()
                .sorted()
                .collect(Collectors.joining());
    }
}
