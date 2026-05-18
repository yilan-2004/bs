package com.agentedu.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.entity.Problem;
import com.agentedu.entity.User;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProblemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String teacherToken;

    private String studentToken;

    private Long teacherId;

    @BeforeEach
    void setUp() throws Exception {
        StpUtil.logout();
        problemMapper.delete(new LambdaQueryWrapper<>());
        userMapper.delete(new LambdaQueryWrapper<>());

        teacherId = insertUser("teacher01", "王老师", "TEACHER");
        insertUser("student01", "张三", "STUDENT");

        teacherToken = login("teacher01", "123456");
        studentToken = login("student01", "123456");
    }

    @Test
    void studentAddProblemShouldFailAndTeacherAddShouldSuccess() throws Exception {
        JsonNode studentResult = postProblem(studentToken, "数组求和", "EASY", "数组,循环");
        assertThat(studentResult.get("code").asInt()).isEqualTo(500);
        assertThat(studentResult.get("message").asText()).contains("教师");

        JsonNode teacherResult = postProblem(teacherToken, "数组求和", "EASY", "数组,循环");
        assertThat(teacherResult.get("code").asInt()).isEqualTo(0);
        assertThat(teacherResult.at("/data").asLong()).isPositive();
    }

    @Test
    void teacherAddProblemThenListCanFindIt() throws Exception {
        postProblem(teacherToken, "字符串反转", "EASY", "字符串");

        JsonNode listResult = getJson("/problem/list?title=字符串&pageNum=1&pageSize=10", teacherToken);
        assertThat(listResult.get("code").asInt()).isEqualTo(0);
        assertThat(listResult.at("/data/total").asLong()).isEqualTo(1);
        assertThat(listResult.at("/data/records/0/title").asText()).isEqualTo("字符串反转");
    }

    @Test
    void studentCanSeeEnabledProblemButCannotSeeDisabledProblem() throws Exception {
        Long enabledId = addProblemDirectly("启用题目", "EASY", "数组");
        Long disabledId = addProblemDirectly("禁用题目", "MEDIUM", "循环");
        disableProblemDirectly(disabledId);

        JsonNode listResult = getJson("/problem/list?pageNum=1&pageSize=10", studentToken);
        assertThat(listResult.at("/data/total").asLong()).isEqualTo(1);
        assertThat(listResult.at("/data/records/0/id").asLong()).isEqualTo(enabledId);

        JsonNode detailResult = getJson("/problem/detail/" + disabledId, studentToken);
        assertThat(detailResult.get("code").asInt()).isEqualTo(500);
    }

    @Test
    void teacherCanUpdateOwnProblem() throws Exception {
        Long problemId = addProblemDirectly("旧标题", "EASY", "数组");

        JsonNode updateResult = putJson("/problem/update", teacherToken, Map.of(
                "id", problemId,
                "title", "新标题",
                "description", "修改后的题目描述",
                "difficulty", "MEDIUM",
                "knowledgeTags", "数组,二分",
                "status", 1
        ));
        assertThat(updateResult.get("code").asInt()).isEqualTo(0);

        Problem updated = problemMapper.selectById(problemId);
        assertThat(updated.getTitle()).isEqualTo("新标题");
        assertThat(updated.getDifficulty()).isEqualTo("MEDIUM");
    }

    @Test
    void keywordDifficultyTagAndPaginationShouldWork() throws Exception {
        addProblemDirectly("数组求和", "EASY", "数组,循环");
        addProblemDirectly("二分查找", "MEDIUM", "数组,二分");
        addProblemDirectly("动态规划入门", "HARD", "动态规划");

        JsonNode keywordResult = getJson("/problem/list?title=数组&pageNum=1&pageSize=10", teacherToken);
        assertThat(keywordResult.at("/data/total").asLong()).isEqualTo(1);
        assertThat(keywordResult.at("/data/records/0/title").asText()).isEqualTo("数组求和");

        JsonNode difficultyResult = getJson("/problem/list?difficulty=MEDIUM&pageNum=1&pageSize=10", teacherToken);
        assertThat(difficultyResult.at("/data/total").asLong()).isEqualTo(1);
        assertThat(difficultyResult.at("/data/records/0/title").asText()).isEqualTo("二分查找");

        JsonNode tagResult = getJson("/problem/list?knowledgeTags=数组&pageNum=1&pageSize=10", teacherToken);
        assertThat(tagResult.at("/data/total").asLong()).isEqualTo(2);

        JsonNode pageResult = getJson("/problem/list?pageNum=1&pageSize=2", teacherToken);
        assertThat(pageResult.at("/data/total").asLong()).isEqualTo(3);
        assertThat(pageResult.at("/data/records").size()).isEqualTo(2);
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

    private String login(String username, String password) throws Exception {
        JsonNode jsonNode = postJson("/auth/login", null, Map.of("username", username, "password", password));
        assertThat(jsonNode.get("code").asInt()).isEqualTo(0);
        return jsonNode.at("/data/token").asText();
    }

    private JsonNode postProblem(String token, String title, String difficulty, String knowledgeTags) throws Exception {
        return postJson("/problem/add", token, Map.of(
                "title", title,
                "description", "题目描述",
                "inputDescription", "输入说明",
                "outputDescription", "输出说明",
                "sampleInput", "1 2",
                "sampleOutput", "3",
                "difficulty", difficulty,
                "knowledgeTags", knowledgeTags
        ));
    }

    private Long addProblemDirectly(String title, String difficulty, String knowledgeTags) {
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setDescription("题目描述");
        problem.setDifficulty(difficulty);
        problem.setKnowledgeTags(knowledgeTags);
        problem.setCreatorId(teacherId);
        problem.setStatus(1);
        problemMapper.insert(problem);
        return problem.getId();
    }

    private void disableProblemDirectly(Long problemId) {
        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setStatus(0);
        problemMapper.updateById(problem);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header("satoken", token))
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

    private JsonNode putJson(String url, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
