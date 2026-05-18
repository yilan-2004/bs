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

@Service
@RequiredArgsConstructor
public class ChoiceEvaluateService {

    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 第一版只支持单选题，答案内容使用选项标识，如 A/B/C。
     */
    public JudgeResult evaluate(Problem problem, String studentAnswer) {
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
        if (correctOptions.size() != 1) {
            throw new BusinessException("选择题正确选项配置不正确");
        }

        String normalizedAnswer = normalize(studentAnswer);
        String correctAnswer = normalize(correctOptions.get(0).getOptionKey());
        boolean accepted = StringUtils.hasText(normalizedAnswer) && normalizedAnswer.equalsIgnoreCase(correctAnswer);
        return buildResult(problem, normalizedAnswer, correctAnswer, accepted);
    }

    private JudgeResult buildResult(Problem problem, String studentAnswer, String correctAnswer, boolean accepted) {
        JudgeResult result = new JudgeResult();
        result.setJudgeStatus(accepted ? JudgeStatusEnum.ACCEPTED.name() : JudgeStatusEnum.WRONG_ANSWER.name());
        result.setPassCount(accepted ? 1 : 0);
        result.setTotalCount(1);
        result.setRunTime(0L);
        result.setNeedAiFeedback(accepted ? 0 : 1);
        result.setOutputResult(studentAnswer);
        if (!accepted) {
            result.setErrorMessage("选择题答案错误");
            result.setErrorFingerprint(HashUtils.md5(problem.getId() + "CHOICE" + studentAnswer + correctAnswer));
        }

        TestCaseJudgeResult caseResult = new TestCaseJudgeResult();
        caseResult.setTestCaseId(0L);
        caseResult.setInputData("学生答案：" + studentAnswer);
        caseResult.setExpectedOutput(correctAnswer);
        caseResult.setActualOutput(studentAnswer);
        caseResult.setErrorOutput(accepted ? "" : "选择题答案错误");
        caseResult.setJudgeStatus(result.getJudgeStatus());
        caseResult.setRunTime(0L);
        caseResult.setPassFlag(accepted ? 1 : 0);
        result.getTestCaseResults().add(caseResult);
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
