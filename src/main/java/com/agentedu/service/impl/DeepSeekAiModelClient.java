package com.agentedu.service.impl;

import com.agentedu.config.AiProperties;
import com.agentedu.exception.BusinessException;
import com.agentedu.service.AiModelClient;
import com.agentedu.service.PromptBuilder;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.agent.CodeContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
public class DeepSeekAiModelClient implements AiModelClient {

    private static final String API_KEY_ENV = "AI_API_KEY";

    private static final String DEEPSEEK_API_KEY_ENV = "DEEPSEEK_API_KEY";

    private final AiProperties aiProperties;

    private final PromptBuilder promptBuilder;

    private final ObjectMapper objectMapper;

    private final RestClient restClient;

    public DeepSeekAiModelClient(AiProperties aiProperties, PromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 调用 DeepSeek Chat Completions，并将模型内容解析为稳定的反馈对象。
     */
    @Override
    public AgentFeedbackResult chat(CodeContext context) {
        String apiKey = resolveApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("AI 服务暂时不可用，请先配置 DeepSeek API Key");
        }

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", promptBuilder.buildSystemPrompt()),
                        Map.of("role", "user", "content", promptBuilder.buildUserPrompt(context))
                ),
                "temperature", 0.2
        );

        long start = System.currentTimeMillis();
        log.info("DeepSeek call started submitId={}, problemId={}, judgeStatus={}, model={}",
                context.getSubmitId(), context.getProblemId(), context.getJudgeStatus(), aiProperties.getModel());
        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            String content = extractMessageContent(responseBody);
            AgentFeedbackResult result = parseFeedbackResult(content);
            log.info("DeepSeek call succeeded submitId={}, problemId={}, judgeStatus={}, elapsedMs={}",
                    context.getSubmitId(), context.getProblemId(), context.getJudgeStatus(),
                    System.currentTimeMillis() - start);
            return result;
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.warn("DeepSeek call failed submitId={}, problemId={}, judgeStatus={}, elapsedMs={}, reason={}: {}",
                    context.getSubmitId(), context.getProblemId(), context.getJudgeStatus(),
                    System.currentTimeMillis() - start, exception.getClass().getSimpleName(), exception.getMessage());
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }
    }

    private String extractMessageContent(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (!contentNode.isTextual() || !StringUtils.hasText(contentNode.asText())) {
            throw new IllegalArgumentException("DeepSeek response content is empty");
        }
        return contentNode.asText();
    }

    private String resolveApiKey() {
        if (StringUtils.hasText(aiProperties.getApiKey())) {
            return aiProperties.getApiKey().trim();
        }
        String deepSeekApiKey = System.getenv(DEEPSEEK_API_KEY_ENV);
        if (StringUtils.hasText(deepSeekApiKey)) {
            return deepSeekApiKey.trim();
        }
        String apiKey = System.getenv(API_KEY_ENV);
        return StringUtils.hasText(apiKey) ? apiKey.trim() : "";
    }

    private AgentFeedbackResult parseFeedbackResult(String modelContent) throws JsonProcessingException {
        String json = extractJsonObject(modelContent);
        if (json != null) {
            try {
                JsonNode node = objectMapper.readTree(json);
                AgentFeedbackResult result = new AgentFeedbackResult();
                result.setErrorType(text(node, "errorType"));
                result.setDiagnosis(text(node, "diagnosis"));
                result.setExplanation(text(node, "explanation"));
                result.setSuggestion(text(node, "suggestion"));
                result.setEvaluation(text(node, "evaluation"));
                result.setRelatedKnowledge(text(node, "relatedKnowledge"));
                result.setNextPracticeAdvice(text(node, "nextPracticeAdvice"));
                result.setRecommendProblems(text(node, "recommendProblems"));
                result.setScore(intValue(node, "score"));
                fillDefaults(result, modelContent);
                return result;
            } catch (JsonProcessingException exception) {
                return fallbackResult(modelContent);
            }
        }
        return fallbackResult(modelContent);
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isTextual() ? value.asText() : "";
    }

    private Integer intValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isInt() || value.isLong()) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                return Integer.parseInt(value.asText().replaceAll("[^0-9-]", ""));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String extractJsonObject(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return null;
    }

    private AgentFeedbackResult fallbackResult(String content) {
        AgentFeedbackResult result = new AgentFeedbackResult();
        result.setErrorType("AI诊断");
        result.setScore(0);
        result.setDiagnosis(limit(content));
        result.setExplanation("模型返回内容不是标准 JSON，系统已将可用分析内容作为兜底反馈。");
        result.setSuggestion("请先复现失败用例，再检查输入处理、核心逻辑和输出格式。");
        result.setEvaluation("本次反馈格式不够理想，但仍可作为调试参考；必要时可以稍后重试生成。");
        result.setRelatedKnowledge("测试用例分析、调试方法");
        result.setNextPracticeAdvice("选择同类基础题继续练习，并为自己的代码补充边界测试。");
        result.setRecommendProblems("");
        return result;
    }

    private void fillDefaults(AgentFeedbackResult result, String rawContent) {
        if (!StringUtils.hasText(result.getErrorType())) {
            result.setErrorType("AI诊断");
        }
        if (result.getScore() == null) {
            result.setScore(0);
        }
        if (!StringUtils.hasText(result.getDiagnosis())) {
            result.setDiagnosis(limit(rawContent));
        }
        if (!StringUtils.hasText(result.getExplanation())) {
            result.setExplanation("请结合题目要求、失败用例和错误信息理解本次问题。");
        }
        if (!StringUtils.hasText(result.getSuggestion())) {
            result.setSuggestion("先复现失败用例，再逐行检查核心逻辑，不要直接跳到改答案。");
        }
        if (!StringUtils.hasText(result.getEvaluation())) {
            result.setEvaluation("这次提交提供了明确的调试线索，继续用测试用例验证修改方向。");
        }
        if (!StringUtils.hasText(result.getRelatedKnowledge())) {
            result.setRelatedKnowledge("测试用例分析、调试方法");
        }
        if (!StringUtils.hasText(result.getNextPracticeAdvice())) {
            result.setNextPracticeAdvice("继续练习同知识点题目，并尝试设计边界测试。");
        }
        if (result.getRecommendProblems() == null) {
            result.setRecommendProblems("");
        }
    }

    private String limit(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 1000 ? content.substring(0, 1000) : content;
    }
}
