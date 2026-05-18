package com.agentedu.service.impl;

import com.agentedu.dto.AgentAskDTO;
import com.agentedu.exception.BusinessException;
import com.agentedu.service.AiModelClient;
import com.agentedu.service.StudentAgentService;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.agent.CodeContext;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.AgentAskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAgentServiceImpl implements StudentAgentService {

    private static final Pattern PROMPT_INJECTION_PATTERN = Pattern.compile(
            "(ignore\\s+(all\\s+)?previous|system\\s+prompt|developer\\s+message|jailbreak|api\\s*key|泄露|忽略(以上|之前)|系统提示词|开发者指令|越狱)",
            Pattern.CASE_INSENSITIVE);

    private final AiModelClient aiModelClient;

    @Override
    public AgentAskVO ask(AgentAskDTO dto) {
        RoleAuthUtils.requireStudent();
        String question = dto == null ? null : dto.getQuestion();
        if (!StringUtils.hasText(question)) {
            throw new BusinessException("请输入学习问题");
        }
        String sanitizedQuestion = sanitizeQuestion(question);
        if (sanitizedQuestion.length() > 500) {
            throw new BusinessException("问题长度不能超过 500 字");
        }

        CodeContext context = new CodeContext();
        context.setQuestionType("GENERAL_TUTOR");
        context.setJudgeStatus("ASK");
        context.setProblemTitle("学生自由问答");
        context.setProblemDescription(sanitizedQuestion);
        context.setKnowledgeTags("学习方法,错题复盘,知识点理解");

        log.info("Student AI tutor question received length={}", sanitizedQuestion.length());
        AgentFeedbackResult result = aiModelClient.chat(context);

        AgentAskVO vo = new AgentAskVO();
        vo.setRelatedKnowledge(result.getRelatedKnowledge());
        vo.setNextPracticeAdvice(result.getNextPracticeAdvice());
        vo.setAnswer(buildAnswer(result));
        return vo;
    }

    private String sanitizeQuestion(String question) {
        String sanitized = question.trim()
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replaceAll("(?i)<\\s*/?\\s*(script|system|developer|assistant)[^>]*>", "");
        String lowered = sanitized.toLowerCase(Locale.ROOT);
        if (PROMPT_INJECTION_PATTERN.matcher(lowered).find()) {
            throw new BusinessException("问题包含疑似 Prompt 注入内容，请换一种学习问题描述");
        }
        return sanitized;
    }

    private String buildAnswer(AgentFeedbackResult result) {
        if (result == null) {
            return "我暂时没有生成有效回复，请稍后再试。";
        }
        StringBuilder builder = new StringBuilder();
        append(builder, result.getDiagnosis());
        append(builder, result.getExplanation());
        append(builder, result.getSuggestion());
        append(builder, result.getEvaluation());
        append(builder, result.getNextPracticeAdvice());
        return builder.length() == 0 ? "我已经收到你的问题，可以换一种问法继续问我。" : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("\n\n");
        }
        builder.append(value.trim());
    }
}
