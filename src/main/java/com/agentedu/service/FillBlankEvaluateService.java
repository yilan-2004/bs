package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.enums.JudgeStatusEnum;
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
     * 填空题支持多个标准答案，使用英文分号或换行分隔，比较时忽略首尾空格。
     */
    public JudgeResult evaluate(Problem problem, String studentAnswer) {
        List<String> answers = splitStandardAnswers(problem.getStandardAnswer());
        if (answers.isEmpty()) {
            throw new BusinessException("填空题标准答案未配置");
        }
        String normalizedStudentAnswer = normalize(studentAnswer);
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
            result.setErrorMessage("填空题答案不匹配");
            result.setErrorFingerprint(HashUtils.md5(problem.getId() + "FILL_BLANK" + studentAnswer + standardAnswer));
        }

        TestCaseJudgeResult caseResult = new TestCaseJudgeResult();
        caseResult.setTestCaseId(0L);
        caseResult.setInputData("学生答案：" + studentAnswer);
        caseResult.setExpectedOutput(standardAnswer);
        caseResult.setActualOutput(studentAnswer);
        caseResult.setErrorOutput(accepted ? "" : "填空题答案不匹配");
        caseResult.setJudgeStatus(result.getJudgeStatus());
        caseResult.setRunTime(0L);
        caseResult.setPassFlag(accepted ? 1 : 0);
        result.getTestCaseResults().add(caseResult);
        return result;
    }

    private List<String> splitStandardAnswers(String standardAnswer) {
        if (!StringUtils.hasText(standardAnswer)) {
            return List.of();
        }
        return Arrays.stream(standardAnswer.split("[;\\r\\n]+"))
                .map(this::normalize)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
