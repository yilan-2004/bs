package com.agentedu.service.impl;

import com.agentedu.config.JudgeProperties;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.entity.TestCase;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.TestCaseMapper;
import com.agentedu.service.CodeJudgeService;
import com.agentedu.service.judge.JudgeResult;
import com.agentedu.service.judge.TestCaseJudgeResult;
import com.agentedu.utils.CodeSecurityUtils;
import com.agentedu.utils.HashUtils;
import com.agentedu.utils.ProcessUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PythonCodeJudgeServiceImpl implements CodeJudgeService {

    /**
     * Python 简化评测使用静态过滤和进程隔离，生产环境仍建议迁移到容器沙箱。
     */
    private static final int ENABLED_STATUS = 1;

    private final TestCaseMapper testCaseMapper;

    private final JudgeProperties judgeProperties;

    @Override
    public JudgeResult judge(SubmitRecord submitRecord) {
        List<TestCase> testCases = testCaseMapper.selectList(new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getProblemId, submitRecord.getProblemId())
                .eq(TestCase::getStatus, ENABLED_STATUS)
                .orderByAsc(TestCase::getSortOrder)
                .orderByAsc(TestCase::getId));
        if (testCases.isEmpty()) {
            throw new BusinessException("该题暂未配置测试用例，无法评测");
        }

        String dangerousKeyword = CodeSecurityUtils.findDangerousKeyword(submitRecord.getCode());
        if (dangerousKeyword != null) {
            throw new BusinessException("代码包含暂不允许的危险语法或模块");
        }

        Path tempDir = null;
        Path codeFile;
        try {
            Path tempRoot = Path.of(judgeProperties.getTempDir()).toAbsolutePath().normalize();
            Files.createDirectories(tempRoot);
            tempDir = Files.createDirectory(tempRoot.resolve("judge_" + UUID.randomUUID()));
            codeFile = Files.createTempFile(tempDir, "main_", ".py");
            if (!codeFile.toRealPath().startsWith(tempRoot.toRealPath())) {
                throw new BusinessException("临时代码文件路径异常");
            }
            Files.writeString(codeFile, submitRecord.getCode(), StandardCharsets.UTF_8);
            return runTestCases(submitRecord, codeFile, testCases);
        } catch (IOException exception) {
            throw new BusinessException("创建或写入临时代码文件失败");
        } finally {
            cleanupTempDirectory(tempDir);
        }
    }

    private JudgeResult runTestCases(SubmitRecord submitRecord, Path codeFile, List<TestCase> testCases) {
        JudgeResult judgeResult = new JudgeResult();
        judgeResult.setJudgeStatus(JudgeStatusEnum.ACCEPTED.name());
        judgeResult.setTotalCount(testCases.size());

        long totalRunTime = 0L;
        TestCaseJudgeResult firstFailedCase = null;
        for (TestCase testCase : testCases) {
            TestCaseJudgeResult caseResult = runSingleCase(codeFile, testCase);
            judgeResult.getTestCaseResults().add(caseResult);
            totalRunTime += caseResult.getRunTime() == null ? 0L : caseResult.getRunTime();

            if (Integer.valueOf(1).equals(caseResult.getPassFlag())) {
                judgeResult.setPassCount(judgeResult.getPassCount() + 1);
                continue;
            }

            if (firstFailedCase == null) {
                firstFailedCase = caseResult;
                judgeResult.setJudgeStatus(caseResult.getJudgeStatus());
                judgeResult.setErrorMessage(firstNonBlank(caseResult.getErrorOutput(), caseResult.getActualOutput()));
                judgeResult.setOutputResult(caseResult.getActualOutput());
                judgeResult.setErrorFingerprint(buildErrorFingerprint(submitRecord.getProblemId(), caseResult));
            }
        }

        judgeResult.setRunTime(totalRunTime);
        judgeResult.setNeedAiFeedback(JudgeStatusEnum.ACCEPTED.name().equals(judgeResult.getJudgeStatus()) ? 0 : 1);
        if (JudgeStatusEnum.ACCEPTED.name().equals(judgeResult.getJudgeStatus())) {
            judgeResult.setOutputResult(lastOutput(judgeResult.getTestCaseResults()));
        }
        return judgeResult;
    }

    private TestCaseJudgeResult runSingleCase(Path codeFile, TestCase testCase) {
        long startTime = System.currentTimeMillis();
        TestCaseJudgeResult result = new TestCaseJudgeResult();
        result.setTestCaseId(testCase.getId());
        result.setInputData(testCase.getInputData());
        result.setExpectedOutput(testCase.getExpectedOutput());

        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    judgeProperties.getPythonCommand(), "-I", "-S", codeFile.toAbsolutePath().toString());
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            process = processBuilder.start();
            String inputData = testCase.getInputData() == null ? "" : testCase.getInputData();
            process.getOutputStream().write(inputData.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
            process.getOutputStream().close();

            boolean finished = ProcessUtils.waitFor(process, judgeProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
            result.setRunTime(System.currentTimeMillis() - startTime);
            if (!finished) {
                result.setJudgeStatus(JudgeStatusEnum.TIME_LIMIT_EXCEEDED.name());
                result.setErrorOutput("TIME_LIMIT");
                result.setPassFlag(0);
                return result;
            }

            String stdout = ProcessUtils.readLimited(process.getInputStream(), judgeProperties.getMaxOutputLength());
            String stderr = ProcessUtils.readLimited(process.getErrorStream(), judgeProperties.getMaxOutputLength());
            result.setActualOutput(stdout);
            result.setErrorOutput(stderr);
            fillStatusByOutput(result, testCase, stderr, stdout);
            return result;
        } catch (Exception exception) {
            result.setRunTime(System.currentTimeMillis() - startTime);
            result.setJudgeStatus(JudgeStatusEnum.RUNTIME_ERROR.name());
            result.setErrorOutput(exception.getMessage());
            result.setPassFlag(0);
            return result;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private void fillStatusByOutput(TestCaseJudgeResult result, TestCase testCase, String stderr, String stdout) {
        if (StringUtils.hasText(stderr)) {
            if (stderr.contains("SyntaxError") || stderr.contains("IndentationError")) {
                result.setJudgeStatus(JudgeStatusEnum.COMPILE_ERROR.name());
            } else {
                result.setJudgeStatus(JudgeStatusEnum.RUNTIME_ERROR.name());
            }
            result.setPassFlag(0);
            return;
        }

        String actual = stdout == null ? "" : stdout.trim();
        String expected = testCase.getExpectedOutput() == null ? "" : testCase.getExpectedOutput().trim();
        if (!actual.equals(expected)) {
            result.setJudgeStatus(JudgeStatusEnum.WRONG_ANSWER.name());
            result.setPassFlag(0);
            return;
        }

        result.setJudgeStatus(JudgeStatusEnum.ACCEPTED.name());
        result.setPassFlag(1);
    }

    private String buildErrorFingerprint(Long problemId, TestCaseJudgeResult caseResult) {
        String status = caseResult.getJudgeStatus();
        if (JudgeStatusEnum.WRONG_ANSWER.name().equals(status)) {
            return HashUtils.md5(problemId + status + caseResult.getTestCaseId()
                    + nullToEmpty(caseResult.getExpectedOutput()) + nullToEmpty(caseResult.getActualOutput()));
        }
        if (JudgeStatusEnum.RUNTIME_ERROR.name().equals(status) || JudgeStatusEnum.COMPILE_ERROR.name().equals(status)) {
            return HashUtils.md5(problemId + status + caseResult.getTestCaseId()
                    + normalizeErrorMessage(caseResult.getErrorOutput()));
        }
        if (JudgeStatusEnum.TIME_LIMIT_EXCEEDED.name().equals(status)) {
            return HashUtils.md5(problemId + status + caseResult.getTestCaseId() + "TIME_LIMIT");
        }
        return null;
    }

    private String normalizeErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "";
        }
        String normalized = errorMessage.replace('\\', '/')
                .replaceAll("File \"[^\"]+\"", "File \"<temp>\"");
        return normalized.length() > 300 ? normalized.substring(0, 300) : normalized;
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return second;
    }

    private String lastOutput(List<TestCaseJudgeResult> results) {
        if (results.isEmpty()) {
            return null;
        }
        return results.get(results.size() - 1).getActualOutput();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir == null || !Files.exists(tempDir)) {
            return;
        }
        try (var stream = Files.walk(tempDir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // 临时目录清理失败不影响评测结果。
                }
            });
        } catch (IOException ignored) {
            // 临时目录清理失败不影响评测结果。
        }
    }
}
