package com.agentedu.service;

import com.agentedu.config.AiProperties;
import com.agentedu.exception.BusinessException;
import com.agentedu.vo.StudentAiAnalysisVO;
import com.agentedu.vo.StudentBankProgressVO;
import com.agentedu.vo.StudentErrorStatsVO;
import com.agentedu.vo.StudentKnowledgeVO;
import com.agentedu.vo.StudentOverviewVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAiAnalysisService {

    private static final String API_KEY_ENV = "AI_API_KEY";

    private final ReportService reportService;

    private final AiProperties aiProperties;

    private final ObjectMapper objectMapper;

    /**
     * 汇总学生报告数据，由用户手动触发 DeepSeek 生成学情分析，避免页面打开就产生模型调用。
     */
    public StudentAiAnalysisVO analyzeStudentReport() {
        StudentOverviewVO overview = reportService.getStudentOverview();
        List<StudentKnowledgeVO> knowledge = reportService.getStudentKnowledgeStats();
        List<StudentErrorStatsVO> errors = reportService.getStudentErrorStats();
        List<StudentBankProgressVO> banks = reportService.getStudentBankProgress();

        if (!Boolean.TRUE.equals(aiProperties.getEnabled())
                || !"deepseek".equalsIgnoreCase(aiProperties.getProvider())) {
            return fallback(overview, knowledge, errors, banks, false);
        }

        String apiKey = System.getenv(API_KEY_ENV);
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(45));
        RestClient restClient = RestClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt()),
                        Map.of("role", "user", "content", userPrompt(overview, knowledge, errors, banks))
                ),
                "temperature", 0.2
        );

        long start = System.currentTimeMillis();
        try {
            log.info("Student report AI analysis started model={}", aiProperties.getModel());
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            StudentAiAnalysisVO result = parseResult(extractContent(responseBody));
            result.setGeneratedByAi(true);
            result.setModel(aiProperties.getModel());
            log.info("Student report AI analysis succeeded elapsedMs={}", System.currentTimeMillis() - start);
            return result;
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.warn("Student report AI analysis failed elapsedMs={}, reason={}: {}",
                    System.currentTimeMillis() - start, exception.getClass().getSimpleName(), exception.getMessage());
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }
    }

    private String systemPrompt() {
        return """
                你是高校多学科学习助教的数据分析智能体。
                请基于学生真实练习数据输出学习分析，不要编造不存在的数据。
                语气要专业、鼓励、可执行，适合本科学生长期学习。
                必须只输出合法 JSON，不要输出 Markdown。
                JSON 字段固定为：
                {
                  "summary": "总体学习情况总结",
                  "strengths": "优势表现",
                  "weaknesses": "主要短板",
                  "recommendations": "改进建议",
                  "nextWeekPlan": "未来一周学习计划",
                  "riskLevel": "LOW/MEDIUM/HIGH"
                }
                """;
    }

    private String userPrompt(StudentOverviewVO overview,
                              List<StudentKnowledgeVO> knowledge,
                              List<StudentErrorStatsVO> errors,
                              List<StudentBankProgressVO> banks) {
        return """
                请分析以下学生学习数据：
                学习概览：
                - 总提交次数：%d
                - 通过次数：%d
                - 错误次数：%d
                - 通过率：%d%%
                - AI 诊断次数：%d
                - 缓存命中次数：%d

                薄弱知识点统计：%s
                常见错误类型：%s
                题库完成进度：%s

                请给出面向学生的学情分析和下一周计划。建议要具体，但不要虚构具体题目答案。
                """.formatted(
                overview.getSubmitCount(),
                overview.getAcceptedCount(),
                overview.getWrongCount(),
                overview.getAcceptedRate(),
                overview.getAiFeedbackCount(),
                overview.getCacheHitCount(),
                limitJson(knowledge),
                limitJson(errors),
                limitJson(banks)
        );
    }

    private String extractContent(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
            throw new IllegalArgumentException("AI response content is empty");
        }
        return content.asText();
    }

    private StudentAiAnalysisVO parseResult(String content) throws JsonProcessingException {
        String json = extractJson(content);
        JsonNode node = objectMapper.readTree(json);
        StudentAiAnalysisVO vo = new StudentAiAnalysisVO();
        vo.setSummary(text(node, "summary", "已生成学习情况分析。"));
        vo.setStrengths(text(node, "strengths", "你已经积累了一定练习记录，建议继续保持。"));
        vo.setWeaknesses(text(node, "weaknesses", "请重点关注错误较多的知识点。"));
        vo.setRecommendations(text(node, "recommendations", "建议按知识点进行专项练习，并及时复盘错题。"));
        vo.setNextWeekPlan(text(node, "nextWeekPlan", "未来一周建议保持稳定练习节奏。"));
        vo.setRiskLevel(text(node, "riskLevel", "LOW"));
        return vo;
    }

    private String extractJson(String content) {
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("AI response is not JSON");
        }
        return cleaned.substring(start, end + 1);
    }

    private String text(JsonNode node, String fieldName, String defaultValue) {
        JsonNode value = node.path(fieldName);
        return value.isTextual() && StringUtils.hasText(value.asText()) ? value.asText() : defaultValue;
    }

    private String limitJson(Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            return json.length() > 1500 ? json.substring(0, 1500) : json;
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private StudentAiAnalysisVO fallback(StudentOverviewVO overview,
                                         List<StudentKnowledgeVO> knowledge,
                                         List<StudentErrorStatsVO> errors,
                                         List<StudentBankProgressVO> banks,
                                         boolean generatedByAi) {
        StudentAiAnalysisVO vo = new StudentAiAnalysisVO();
        vo.setSummary("当前累计提交 " + overview.getSubmitCount() + " 次，通过率 " + overview.getAcceptedRate() + "%。");
        vo.setStrengths(overview.getAcceptedCount() > 0 ? "已经有题目通过记录，说明基础练习链路较稳定。" : "已开始建立练习记录，后续可以逐步提高通过率。");
        vo.setWeaknesses(knowledge.isEmpty() ? "暂无足够知识点数据，需要继续练习沉淀样本。" : "优先复盘错误较多的知识点：" + knowledge.get(0).getKnowledgeTag() + "。");
        vo.setRecommendations(errors.isEmpty() ? "建议先完成基础题库练习，并及时查看评测反馈。" : "建议结合常见错误类型复盘，先复现错误，再修改思路。");
        vo.setNextWeekPlan(banks.isEmpty() ? "未来一周建议每天完成 2-3 道基础练习题。" : "未来一周建议围绕完成度较低的题库进行专项练习。");
        vo.setRiskLevel(overview.getAcceptedRate() != null && overview.getAcceptedRate() < 50 ? "MEDIUM" : "LOW");
        vo.setGeneratedByAi(generatedByAi);
        vo.setModel(Boolean.TRUE.equals(aiProperties.getEnabled()) ? aiProperties.getModel() : "local-rule");
        return vo;
    }
}
