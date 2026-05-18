package com.agentedu.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.entity.AiFeedback;
import com.agentedu.entity.Problem;
import com.agentedu.entity.SubmitCaseResult;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.entity.User;
import com.agentedu.mapper.AiFeedbackMapper;
import com.agentedu.mapper.AiErrorCacheMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.SubmitCaseResultMapper;
import com.agentedu.mapper.SubmitRecordMapper;
import com.agentedu.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiFeedbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private SubmitRecordMapper submitRecordMapper;

    @Autowired
    private SubmitCaseResultMapper submitCaseResultMapper;

    @Autowired
    private AiFeedbackMapper aiFeedbackMapper;

    @Autowired
    private AiErrorCacheMapper aiErrorCacheMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long teacherId;

    private Long studentId;

    private Long otherStudentId;

    private Long problemId;

    private String teacherToken;

    private String studentToken;

    private String otherStudentToken;

    @BeforeEach
    void setUp() throws Exception {
        StpUtil.logout();
        aiFeedbackMapper.delete(new LambdaQueryWrapper<>());
        aiErrorCacheMapper.delete(new LambdaQueryWrapper<>());
        submitCaseResultMapper.delete(new LambdaQueryWrapper<>());
        submitRecordMapper.delete(new LambdaQueryWrapper<>());
        problemMapper.delete(new LambdaQueryWrapper<>());
        userMapper.delete(new LambdaQueryWrapper<>());

        teacherId = insertUser("teacher_ai", "Teacher AI", "TEACHER");
        studentId = insertUser("student_ai", "Student AI", "STUDENT");
        otherStudentId = insertUser("student_other", "Other Student", "STUDENT");
        problemId = insertProblem("Sum A B");

        teacherToken = login("teacher_ai");
        studentToken = login("student_ai");
        otherStudentToken = login("student_other");
    }

    @Test
    void anonymousTeacherAndOtherStudentCannotGenerateFeedback() throws Exception {
        Long submitId = insertSubmit(studentId, "WRONG_ANSWER", "print(0)", "wrong output");
        insertFailedCase(submitId, "WRONG_ANSWER", "1 2", "3", "0", "");

        assertThat(postFeedback(null, submitId).get("code").asInt()).isNotEqualTo(0);
        assertThat(postFeedback(teacherToken, submitId).get("code").asInt()).isEqualTo(500);
        assertThat(postFeedback(otherStudentToken, submitId).get("code").asInt()).isEqualTo(500);
    }

    @Test
    void missingSubmitIdShouldFail() throws Exception {
        JsonNode result = postFeedback(studentToken, 999999L);

        assertThat(result.get("code").asInt()).isEqualTo(500);
        assertThat(result.get("message").asText()).contains("提交记录不存在");
    }

    @Test
    void acceptedSubmitShouldReturnAcceptedFeedbackAndPersist() throws Exception {
        Long submitId = insertSubmit(studentId, "ACCEPTED", "print(3)", null);

        JsonNode result = postFeedback(studentToken, submitId);

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/errorType").asText()).contains("通过");
        assertThat(result.at("/data/aiModel").asText()).isEqualTo("mock-ai");
        assertThat(result.at("/data/fromCache").asBoolean()).isFalse();
        assertFeedbackPersisted(submitId, "通过");
        assertThat(aiErrorCacheMapper.selectCount(new LambdaQueryWrapper<>())).isZero();
    }

    @Test
    void wrongAnswerShouldReturnLogicErrorFeedback() throws Exception {
        Long submitId = insertSubmit(studentId, "WRONG_ANSWER", "print(0)", "0");
        insertFailedCase(submitId, "WRONG_ANSWER", "1 2", "3", "0", "");

        JsonNode result = postFeedback(studentToken, submitId);

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/errorType").asText()).isEqualTo("逻辑错误");
        assertThat(result.at("/data/diagnosis").asText()).contains("实际输出与期望输出不一致");
    }

    @Test
    void runtimeErrorShouldReturnRuntimeFeedback() throws Exception {
        Long submitId = insertSubmit(studentId, "RUNTIME_ERROR", "print(1 / 0)", "ZeroDivisionError");
        insertFailedCase(submitId, "RUNTIME_ERROR", "", "", "", "ZeroDivisionError: division by zero");

        JsonNode result = postFeedback(studentToken, submitId);

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/errorType").asText()).isEqualTo("运行时错误");
        assertThat(result.at("/data/suggestion").asText()).contains("变量");
    }

    @Test
    void compileErrorShouldReturnSyntaxFeedback() throws Exception {
        Long submitId = insertSubmit(studentId, "COMPILE_ERROR", "if True\n    print(1)", "SyntaxError");
        insertFailedCase(submitId, "COMPILE_ERROR", "", "", "", "SyntaxError: expected ':'");

        JsonNode result = postFeedback(studentToken, submitId);

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/errorType").asText()).isEqualTo("语法错误");
        assertThat(result.at("/data/suggestion").asText()).contains("缩进");
    }

    @Test
    void timeLimitExceededShouldReturnTimeoutFeedback() throws Exception {
        Long submitId = insertSubmit(studentId, "TIME_LIMIT_EXCEEDED", "while True:\n    pass", "TIME_LIMIT");
        insertFailedCase(submitId, "TIME_LIMIT_EXCEEDED", "", "", "", "TIME_LIMIT");

        JsonNode result = postFeedback(studentToken, submitId);

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/errorType").asText()).isEqualTo("运行超时");
        assertThat(result.at("/data/suggestion").asText()).contains("死循环");
    }

    @Test
    void secondRequestShouldReuseExistingFeedback() throws Exception {
        Long submitId = insertSubmit(studentId, "WRONG_ANSWER", "print(0)", "0");
        insertFailedCase(submitId, "WRONG_ANSWER", "1 2", "3", "0", "");

        JsonNode first = postFeedback(studentToken, submitId);
        JsonNode second = postFeedback(studentToken, submitId);

        assertThat(first.get("code").asInt()).isEqualTo(0);
        assertThat(second.get("code").asInt()).isEqualTo(0);
        assertThat(second.at("/data/id").asLong()).isEqualTo(first.at("/data/id").asLong());
        assertThat(aiFeedbackMapper.selectCount(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getSubmitId, submitId))).isEqualTo(1L);
    }

    @Test
    void sameErrorShouldReuseAiErrorCacheForSecondStudent() throws Exception {
        String fingerprint = "abcdef0123456789abcdef0123456789";
        Long firstSubmitId = insertSubmit(studentId, "WRONG_ANSWER", "print(0)", "0", fingerprint);
        insertFailedCase(firstSubmitId, "WRONG_ANSWER", "1 2", "3", "0", "");

        JsonNode first = postFeedback(studentToken, firstSubmitId);
        assertThat(first.get("code").asInt()).isEqualTo(0);
        assertThat(first.at("/data/fromCache").asBoolean()).isFalse();
        assertThat(first.at("/data/cacheId").isNull()).isTrue();
        assertThat(aiErrorCacheMapper.selectCount(new LambdaQueryWrapper<>())).isEqualTo(1L);

        Long secondSubmitId = insertSubmit(otherStudentId, "WRONG_ANSWER", "print(0)", "0", fingerprint);
        insertFailedCase(secondSubmitId, "WRONG_ANSWER", "1 2", "3", "0", "");

        JsonNode second = postFeedback(otherStudentToken, secondSubmitId);
        assertThat(second.get("code").asInt()).isEqualTo(0);
        assertThat(second.at("/data/fromCache").asBoolean()).isTrue();
        assertThat(second.at("/data/cacheHit").asBoolean()).isTrue();
        assertThat(second.at("/data/cacheId").asLong()).isPositive();
        assertThat(second.at("/data/reuseCount").asInt()).isEqualTo(1);
        assertThat(second.at("/data/errorType").asText()).isEqualTo("逻辑错误");
        assertThat(aiErrorCacheMapper.selectCount(new LambdaQueryWrapper<>())).isEqualTo(1L);
    }

    @Test
    void acceptedSubmitShouldNotUseAiErrorCacheEvenWithFingerprint() throws Exception {
        Long submitId = insertSubmit(studentId, "ACCEPTED", "print(3)", null, "1234567890abcdef1234567890abcdef");

        JsonNode result = postFeedback(studentToken, submitId);

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/fromCache").asBoolean()).isFalse();
        assertThat(aiErrorCacheMapper.selectCount(new LambdaQueryWrapper<>())).isZero();
    }

    private void assertFeedbackPersisted(Long submitId, String expectedErrorType) {
        AiFeedback feedback = aiFeedbackMapper.selectOne(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getSubmitId, submitId));
        assertThat(feedback).isNotNull();
        assertThat(feedback.getErrorType()).contains(expectedErrorType);
        assertThat(feedback.getFromCache()).isZero();
        assertThat(feedback.getCacheId()).isNull();
        assertThat(feedback.getAiModel()).isEqualTo("mock-ai");
    }

    private Long insertUser(String username, String realName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);
        return user.getId();
    }

    private Long insertProblem(String title) {
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setDescription("Read two integers and output their sum.");
        problem.setDifficulty("EASY");
        problem.setKnowledgeTags("basic,io");
        problem.setCreatorId(teacherId);
        problem.setStatus(1);
        problemMapper.insert(problem);
        return problem.getId();
    }

    private Long insertSubmit(Long userId, String judgeStatus, String code, String errorMessage) {
        return insertSubmit(userId, judgeStatus, code, errorMessage, null);
    }

    private Long insertSubmit(Long userId, String judgeStatus, String code, String errorMessage, String errorFingerprint) {
        SubmitRecord record = new SubmitRecord();
        record.setUserId(userId);
        record.setProblemId(problemId);
        record.setLanguage("python");
        record.setCode(code);
        record.setJudgeStatus(judgeStatus);
        record.setPassCount("ACCEPTED".equals(judgeStatus) ? 1 : 0);
        record.setTotalCount(1);
        record.setRunTime(1L);
        record.setNeedAiFeedback("ACCEPTED".equals(judgeStatus) ? 0 : 1);
        record.setCodeHash("hash-" + judgeStatus);
        record.setErrorMessage(errorMessage);
        record.setErrorFingerprint(errorFingerprint);
        submitRecordMapper.insert(record);
        return record.getId();
    }

    private void insertFailedCase(Long submitId, String judgeStatus, String input, String expected, String actual, String error) {
        SubmitCaseResult result = new SubmitCaseResult();
        result.setSubmitId(submitId);
        result.setTestCaseId(1L);
        result.setInputData(input);
        result.setExpectedOutput(expected);
        result.setActualOutput(actual);
        result.setErrorOutput(error);
        result.setJudgeStatus(judgeStatus);
        result.setRunTime(1L);
        result.setPassFlag(0);
        submitCaseResultMapper.insert(result);
    }

    private String login(String username) throws Exception {
        JsonNode jsonNode = postJson("/auth/login", null, Map.of("username", username, "password", "123456"));
        assertThat(jsonNode.get("code").asInt()).isEqualTo(0);
        return jsonNode.at("/data/token").asText();
    }

    private JsonNode postFeedback(String token, Long submitId) throws Exception {
        return postJson("/ai/feedback/" + submitId, token, Map.of());
    }

    private JsonNode postJson(String url, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("satoken", token == null ? "" : token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
