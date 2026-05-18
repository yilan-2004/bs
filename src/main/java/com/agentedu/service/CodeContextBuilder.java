package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.entity.SubmitCaseResult;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.enums.QuestionTypeEnum;
import com.agentedu.service.agent.CodeContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeContextBuilder {

    private static final int FULL_CODE_LIMIT = 8000;

    private static final int DEFAULT_HEAD_LINES = 120;

    private static final int ERROR_CONTEXT_LINES = 20;

    private static final Pattern LINE_PATTERN = Pattern.compile("line\\s+(\\d+)");

    /**
     * 根据提交、题目和失败用例构建 AI 分析上下文。
     */
    public CodeContext build(SubmitRecord submitRecord, Problem problem, SubmitCaseResult failedCaseResult) {
        CodeContext context = new CodeContext();
        context.setSubmitId(submitRecord.getId());
        context.setProblemId(problem.getId());
        context.setSubjectId(problem.getSubjectId());
        context.setProblemTitle(problem.getTitle());
        context.setProblemDescription(problem.getDescription());
        context.setKnowledgeTags(problem.getKnowledgeTags());
        context.setQuestionType(problem.getQuestionType());
        context.setJudgeStatus(submitRecord.getJudgeStatus());
        context.setErrorMessage(submitRecord.getErrorMessage());
        if (failedCaseResult != null) {
            context.setFailedInput(failedCaseResult.getInputData());
            context.setExpectedOutput(failedCaseResult.getExpectedOutput());
            context.setActualOutput(failedCaseResult.getActualOutput());
            context.setStudentAnswer(failedCaseResult.getActualOutput());
            context.setCorrectAnswer(failedCaseResult.getExpectedOutput());
            if (!StringUtils.hasText(context.getErrorMessage())) {
                context.setErrorMessage(failedCaseResult.getErrorOutput());
            }
        }
        if (QuestionTypeEnum.PROGRAMMING.name().equals(problem.getQuestionType()) || problem.getQuestionType() == null) {
            context.setKeyCodeSnippet(extractKeyCodeSnippet(submitRecord.getCode(), submitRecord.getJudgeStatus(), context.getErrorMessage()));
        } else {
            context.setStudentAnswer(submitRecord.getCode());
            context.setKeyCodeSnippet("");
        }
        return context;
    }

    private String extractKeyCodeSnippet(String code, String judgeStatus, String errorMessage) {
        if (code == null) {
            return "";
        }
        if (code.length() <= FULL_CODE_LIMIT) {
            return code;
        }
        if (JudgeStatusEnum.RUNTIME_ERROR.name().equals(judgeStatus) || JudgeStatusEnum.COMPILE_ERROR.name().equals(judgeStatus)) {
            Integer lineNumber = extractLineNumber(errorMessage);
            if (lineNumber != null) {
                return sliceAroundLine(code, lineNumber, ERROR_CONTEXT_LINES);
            }
            return firstLines(code, DEFAULT_HEAD_LINES);
        }
        if (JudgeStatusEnum.TIME_LIMIT_EXCEEDED.name().equals(judgeStatus)) {
            String loopSnippet = extractLoopOrRecursiveSnippet(code);
            if (StringUtils.hasText(loopSnippet)) {
                return loopSnippet;
            }
            return firstLines(code, DEFAULT_HEAD_LINES);
        }
        return firstLines(code, DEFAULT_HEAD_LINES);
    }

    private Integer extractLineNumber(String errorMessage) {
        if (!StringUtils.hasText(errorMessage)) {
            return null;
        }
        Matcher matcher = LINE_PATTERN.matcher(errorMessage);
        Integer lineNumber = null;
        while (matcher.find()) {
            lineNumber = Integer.parseInt(matcher.group(1));
        }
        return lineNumber;
    }

    private String sliceAroundLine(String code, int lineNumber, int radius) {
        String[] lines = code.split("\\R", -1);
        int start = Math.max(0, lineNumber - radius - 1);
        int end = Math.min(lines.length, lineNumber + radius);
        return String.join(System.lineSeparator(), Arrays.copyOfRange(lines, start, end));
    }

    private String firstLines(String code, int count) {
        String[] lines = code.split("\\R", -1);
        int end = Math.min(lines.length, count);
        return String.join(System.lineSeparator(), Arrays.copyOfRange(lines, 0, end));
    }

    private String extractLoopOrRecursiveSnippet(String code) {
        String[] lines = code.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.startsWith("for ") || trimmed.startsWith("while ") || isRecursiveFunctionLine(trimmed, lines)) {
                int start = Math.max(0, i - 10);
                int end = Math.min(lines.length, i + 40);
                return String.join(System.lineSeparator(), Arrays.copyOfRange(lines, start, end));
            }
        }
        return "";
    }

    private boolean isRecursiveFunctionLine(String line, String[] lines) {
        if (!line.startsWith("def ") || !line.contains("(")) {
            return false;
        }
        String name = line.substring(4, line.indexOf('(')).trim();
        if (!StringUtils.hasText(name)) {
            return false;
        }
        return Arrays.stream(lines).anyMatch(item -> item.trim().startsWith(name + "(") || item.contains(" " + name + "("));
    }
}
