package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.enums.QuestionTypeEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.service.judge.JudgeResult;
import com.agentedu.service.judge.TestCaseJudgeResult;
import com.agentedu.utils.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class FillBlankEvaluateService {

    /**
     * 填空题/判断题轻量评测。
     * 标准答案支持英文分号或换行分隔；比较时忽略首尾空格。
     */
    public JudgeResult evaluate(Problem problem, String studentAnswer) {
        List<String> answers = splitStandardAnswers(problem.getStandardAnswer(), problem.getQuestionType());
        if (answers.isEmpty()) {
            throw new BusinessException("标准答案未配置");
        }
        String normalizedStudentAnswer = normalize(studentAnswer, problem.getQuestionType());
        boolean accepted = answers.stream().anyMatch(answer -> answer.equals(normalizedStudentAnswer));
        return buildResult(problem, normalizedStudentAnswer, String.join(";", answers), accepted);
    }

    private JudgeResult buildResult(Problem problem, String studentAnswer, String standardAnswer, boolean accepted) {
        JudgeResult result = new JudgeResult();
        result.setJudgeStatus(accepted ? JudgeStatusEnum.ACCEPTED.name() : JudgeStatusEnum.WRONG_ANSWER.name());
        result.setPassCount(accepted ? 1 : 0);
        result.setTotalCount(1);
        result.setRunTime(0L);
        result.setNeedAiFeedback(accepted ? 0 : 1);
        result.setOutputResult(studentAnswer);
        if (!accepted) {
            result.setErrorMessage(errorMessage(problem.getQuestionType()));
            String type = StringUtils.hasText(problem.getQuestionType())
                    ? problem.getQuestionType()
                    : QuestionTypeEnum.FILL_BLANK.name();
            result.setErrorFingerprint(HashUtils.md5(problem.getId() + type + studentAnswer + standardAnswer));
        }

        TestCaseJudgeResult caseResult = new TestCaseJudgeResult();
        caseResult.setTestCaseId(0L);
        caseResult.setInputData("学生答案：" + studentAnswer);
        caseResult.setExpectedOutput(standardAnswer);
        caseResult.setActualOutput(studentAnswer);
        caseResult.setErrorOutput(accepted ? "" : result.getErrorMessage());
        caseResult.setJudgeStatus(result.getJudgeStatus());
        caseResult.setRunTime(0L);
        caseResult.setPassFlag(accepted ? 1 : 0);
        result.getTestCaseResults().add(caseResult);
        return result;
    }

    private List<String> splitStandardAnswers(String standardAnswer, String questionType) {
        if (!StringUtils.hasText(standardAnswer)) {
            return List.of();
        }
        return Arrays.stream(standardAnswer.split("[;\\r\\n]+"))
                .map(answer -> normalize(answer, questionType))
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String normalize(String value, String questionType) {
        String normalized = value == null ? "" : value.trim();
        if (QuestionTypeEnum.TRUE_FALSE.name().equals(questionType)) {
            return switch (normalized) {
                case "对", "正确", "true", "TRUE", "True", "√", "✓" -> "√";
                case "错", "错误", "false", "FALSE", "False", "×", "x", "X" -> "×";
                default -> normalized;
            };
        }
        return normalized;
    }

    private String errorMessage(String questionType) {
        if (QuestionTypeEnum.TRUE_FALSE.name().equals(questionType)) {
            return "判断题答案错误";
        }
        return "填空题答案不匹配";
    }
}
