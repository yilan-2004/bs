package com.agentedu.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.entity.Problem;
import com.agentedu.entity.QuestionOption;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.entity.TestCase;
import com.agentedu.entity.User;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.QuestionOptionMapper;
import com.agentedu.mapper.SubmitRecordMapper;
import com.agentedu.mapper.TestCaseMapper;
import com.agentedu.mapper.UserMapper;
import com.agentedu.utils.HashUtils;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubmitControllerIntegrationTest {

    private static final String PYTHON_CODE = "a, b = map(int, input().split())\nprint(a + b)";

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
    private TestCaseMapper testCaseMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long teacherId;

    private Long otherTeacherId;

    private Long studentId;

    private Long otherStudentId;

    private Long enabledProblemId;

    private Long disabledProblemId;

    private Long otherTeacherProblemId;

    private String teacherToken;

    private String otherTeacherToken;

    private String studentToken;

    private String otherStudentToken;

    @BeforeEach
    void setUp() throws Exception {
        StpUtil.logout();
        submitRecordMapper.delete(new LambdaQueryWrapper<>());
        testCaseMapper.delete(new LambdaQueryWrapper<>());
        questionOptionMapper.delete(new LambdaQueryWrapper<>());
        problemMapper.delete(new LambdaQueryWrapper<>());
        userMapper.delete(new LambdaQueryWrapper<>());

        teacherId = insertUser("teacher01", "Teacher One", "TEACHER");
        otherTeacherId = insertUser("teacher02", "Teacher Two", "TEACHER");
        studentId = insertUser("student01", "Student One", "STUDENT");
        otherStudentId = insertUser("student02", "Student Two", "STUDENT");

        enabledProblemId = insertProblem("Sum A B", teacherId, 1);
        disabledProblemId = insertProblem("Disabled Problem", teacherId, 0);
        otherTeacherProblemId = insertProblem("Other Teacher Problem", otherTeacherId, 1);
        insertTestCase(enabledProblemId, "1 2\n", "3", 1);

        teacherToken = login("teacher01");
        otherTeacherToken = login("teacher02");
        studentToken = login("student01");
        otherStudentToken = login("student02");
    }

    @Test
    void anonymousUserCannotSubmitCode() throws Exception {
        JsonNode result = postJson("/submit/code", null, submitBody(enabledProblemId, "python", PYTHON_CODE));

        assertThat(result.get("code").asInt()).isNotEqualTo(0);
        assertThat(submitRecordMapper.selectCount(new LambdaQueryWrapper<>())).isZero();
    }

    @Test
    void teacherCannotSubmitCode() throws Exception {
        JsonNode result = postJson("/submit/code", teacherToken, submitBody(enabledProblemId, "python", PYTHON_CODE));

        assertThat(result.get("code").asInt()).isEqualTo(500);
        assertThat(submitRecordMapper.selectCount(new LambdaQueryWrapper<>())).isZero();
    }

    @Test
    void studentCanSubmitPythonCodeAndRecordIsSavedWithCodeHash() throws Exception {
        JsonNode result = postJson("/submit/code", studentToken, submitBody(enabledProblemId, "python", PYTHON_CODE));

        assertThat(result.get("code").asInt()).isEqualTo(0);
        assertThat(result.at("/data/problemId").asLong()).isEqualTo(enabledProblemId);
        assertThat(result.at("/data/judgeStatus").asText()).isEqualTo("ACCEPTED");
        assertThat(result.at("/data/passCount").asInt()).isEqualTo(1);
        assertThat(result.at("/data/totalCount").asInt()).isEqualTo(1);
        assertThat(result.at("/data/needAiFeedback").asInt()).isZero();
        assertThat(result.at("/data/codeHash").asText()).isEqualTo(HashUtils.md5(PYTHON_CODE));
        assertThat(result.at("/data/testCaseResults").size()).isEqualTo(1);

        Long submitId = result.at("/data/submitId").asLong();
        SubmitRecord record = submitRecordMapper.selectById(submitId);
        assertThat(record).isNotNull();
        assertThat(record.getUserId()).isEqualTo(studentId);
        assertThat(record.getProblemId()).isEqualTo(enabledProblemId);
        assertThat(record.getCodeHash()).isEqualTo(HashUtils.md5(PYTHON_CODE));
        assertThat(record.getNeedAiFeedback()).isZero();
        assertThat(record.getJudgeStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void invalidLanguageEmptyCodeMissingProblemDisabledProblemAndLongCodeShouldFail() throws Exception {
        assertThat(postJson("/submit/code", studentToken, submitBody(enabledProblemId, "java", PYTHON_CODE))
                .get("code").asInt()).isEqualTo(500);

        assertThat(postJson("/submit/code", studentToken, submitBody(enabledProblemId, "python", ""))
                .get("code").asInt()).isEqualTo(500);

        assertThat(postJson("/submit/code", studentToken, submitBody(99999L, "python", PYTHON_CODE))
                .get("code").asInt()).isEqualTo(500);

        assertThat(postJson("/submit/code", studentToken, submitBody(disabledProblemId, "python", PYTHON_CODE))
                .get("code").asInt()).isEqualTo(500);

        String longCode = "x".repeat(50 * 1024 + 1);
        assertThat(postJson("/submit/code", studentToken, submitBody(enabledProblemId, "python", longCode))
                .get("code").asInt()).isEqualTo(500);

        assertThat(submitRecordMapper.selectCount(new LambdaQueryWrapper<>())).isZero();
    }

    @Test
    void studentCanOnlySeeOwnSubmissionRecords() throws Exception {
        Long ownSubmitId = insertSubmit(studentId, enabledProblemId, PYTHON_CODE);
        insertSubmit(otherStudentId, enabledProblemId, "print(2)");

        JsonNode myResult = getJson("/submit/my?pageNum=1&pageSize=10", studentToken);
        assertThat(myResult.get("code").asInt()).isEqualTo(0);
        assertThat(myResult.at("/data/total").asLong()).isEqualTo(1);
        assertThat(myResult.at("/data/records/0/id").asLong()).isEqualTo(ownSubmitId);

        JsonNode forbiddenDetail = getJson("/submit/detail/" + insertSubmit(otherStudentId, enabledProblemId, "print(3)"), studentToken);
        assertThat(forbiddenDetail.get("code").asInt()).isEqualTo(500);
    }

    @Test
    void teacherCanSeeOwnProblemSubmissionsButCannotSeeOthersProblemSubmissions() throws Exception {
        Long ownSubmitId = insertSubmit(studentId, enabledProblemId, PYTHON_CODE);
        Long otherProblemSubmitId = insertSubmit(studentId, otherTeacherProblemId, "print(99)");

        JsonNode ownProblemResult = getJson("/submit/problem/" + enabledProblemId + "?pageNum=1&pageSize=10", teacherToken);
        assertThat(ownProblemResult.get("code").asInt()).isEqualTo(0);
        assertThat(ownProblemResult.at("/data/total").asLong()).isEqualTo(1);
        assertThat(ownProblemResult.at("/data/records/0/id").asLong()).isEqualTo(ownSubmitId);

        JsonNode listResult = getJson("/submit/list?pageNum=1&pageSize=10&problemId=" + enabledProblemId + "&userId=" + studentId + "&judgeStatus=JUDGING", teacherToken);
        assertThat(listResult.get("code").asInt()).isEqualTo(0);
        assertThat(listResult.at("/data/total").asLong()).isEqualTo(1);

        JsonNode forbiddenProblemResult = getJson("/submit/problem/" + otherTeacherProblemId + "?pageNum=1&pageSize=10", teacherToken);
        assertThat(forbiddenProblemResult.get("code").asInt()).isEqualTo(500);

        JsonNode forbiddenListResult = getJson("/submit/list?pageNum=1&pageSize=10&problemId=" + otherTeacherProblemId, teacherToken);
        assertThat(forbiddenListResult.get("code").asInt()).isEqualTo(500);

        JsonNode forbiddenDetail = getJson("/submit/detail/" + otherProblemSubmitId, teacherToken);
        assertThat(forbiddenDetail.get("code").asInt()).isEqualTo(500);

        JsonNode otherTeacherDetail = getJson("/submit/detail/" + otherProblemSubmitId, otherTeacherToken);
        assertThat(otherTeacherDetail.get("code").asInt()).isEqualTo(0);
    }

    @Test
    void choiceAndFillBlankSubmissionsShouldBeEvaluated() throws Exception {
        Long choiceProblemId = insertProblem("Choice Question", teacherId, 1);
        setQuestionType(choiceProblemId, "CHOICE", null);
        insertOption(choiceProblemId, "A", "1", 0, 0);
        insertOption(choiceProblemId, "B", "2", 1, 1);

        JsonNode choiceAccepted = postJson("/submission/submit", studentToken, Map.of(
                "problemId", choiceProblemId,
                "questionType", "CHOICE",
                "answerContent", "B"
        ));
        assertThat(choiceAccepted.at("/data/judgeStatus").asText()).isEqualTo("ACCEPTED");
        assertThat(choiceAccepted.at("/data/needAiFeedback").asInt()).isZero();

        JsonNode choiceWrong = postJson("/submission/submit", studentToken, Map.of(
                "problemId", choiceProblemId,
                "questionType", "CHOICE",
                "answerContent", "A"
        ));
        assertThat(choiceWrong.at("/data/judgeStatus").asText()).isEqualTo("WRONG_ANSWER");
        assertThat(choiceWrong.at("/data/errorFingerprint").asText()).isNotBlank();

        Long fillProblemId = insertProblem("Fill Question", teacherId, 1);
        setQuestionType(fillProblemId, "FILL_BLANK", "Python; python");

        JsonNode fillAccepted = postJson("/submission/submit", studentToken, Map.of(
                "problemId", fillProblemId,
                "questionType", "FILL_BLANK",
                "answerContent", " python "
        ));
        assertThat(fillAccepted.at("/data/judgeStatus").asText()).isEqualTo("ACCEPTED");

        JsonNode fillWrong = postJson("/submission/submit", studentToken, Map.of(
                "problemId", fillProblemId,
                "questionType", "FILL_BLANK",
                "answerContent", "Java"
        ));
        assertThat(fillWrong.at("/data/judgeStatus").asText()).isEqualTo("WRONG_ANSWER");
        assertThat(fillWrong.at("/data/errorFingerprint").asText()).isNotBlank();
    }

    @Test
    void shortAnswerSubmissionShouldBeAiEvaluatedAndReturnFeedback() throws Exception {
        Long problemId = insertProblem("Short Answer Question", teacherId, 1);
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setQuestionType("SHORT_ANSWER");
        problem.setStandardAnswer("Loop executes repeated logic and needs an exit condition to avoid infinite loops.");
        problem.setScoringPoints("repeat logic; exit condition; avoid infinite loop");
        problem.setScore(100);
        problemMapper.updateById(problem);

        JsonNode response = postJson("/submission/submit", studentToken, Map.of(
                "problemId", problemId,
                "questionType", "SHORT_ANSWER",
                "answerContent", "Loop is used to repeat operations. It should have a condition to stop."
        ));

        assertThat(response.at("/data/judgeStatus").asText()).isIn("ACCEPTED", "PARTIAL_ACCEPTED", "WRONG_ANSWER");
        assertThat(response.at("/data/score").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(response.at("/data/aiFeedback/diagnosis").asText()).isNotBlank();
        assertThat(response.at("/data/testCaseResults").size()).isEqualTo(1);
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

    private Long insertProblem(String title, Long creatorId, Integer status) {
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setDescription("Description");
        problem.setDifficulty("EASY");
        problem.setKnowledgeTags("basic");
        problem.setCreatorId(creatorId);
        problem.setStatus(status);
        problemMapper.insert(problem);
        return problem.getId();
    }

    private Long insertSubmit(Long userId, Long problemId, String code) {
        SubmitRecord record = new SubmitRecord();
        record.setUserId(userId);
        record.setProblemId(problemId);
        record.setLanguage("python");
        record.setCode(code);
        record.setJudgeStatus("JUDGING");
        record.setPassCount(0);
        record.setTotalCount(0);
        record.setRunTime(0L);
        record.setNeedAiFeedback(0);
        record.setCodeHash(HashUtils.md5(code));
        submitRecordMapper.insert(record);
        return record.getId();
    }

    private void insertTestCase(Long problemId, String inputData, String expectedOutput, Integer sortOrder) {
        TestCase testCase = new TestCase();
        testCase.setProblemId(problemId);
        testCase.setInputData(inputData);
        testCase.setExpectedOutput(expectedOutput);
        testCase.setIsSample(1);
        testCase.setSortOrder(sortOrder);
        testCase.setStatus(1);
        testCaseMapper.insert(testCase);
    }

    private void setQuestionType(Long problemId, String questionType, String standardAnswer) {
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setQuestionType(questionType);
        problem.setStandardAnswer(standardAnswer);
        problemMapper.updateById(problem);
    }

    private void insertOption(Long problemId, String key, String content, Integer isCorrect, Integer sortOrder) {
        QuestionOption option = new QuestionOption();
        option.setProblemId(problemId);
        option.setOptionKey(key);
        option.setOptionContent(content);
        option.setIsCorrect(isCorrect);
        option.setSortOrder(sortOrder);
        questionOptionMapper.insert(option);
    }

    private String login(String username) throws Exception {
        JsonNode jsonNode = postJson("/auth/login", null, Map.of("username", username, "password", "123456"));
        assertThat(jsonNode.get("code").asInt()).isEqualTo(0);
        return jsonNode.at("/data/token").asText();
    }

    private Map<String, Object> submitBody(Long problemId, String language, String code) {
        return Map.of("problemId", problemId, "language", language, "code", code);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header("satoken", token == null ? "" : token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
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
