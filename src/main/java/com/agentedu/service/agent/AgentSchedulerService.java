package com.agentedu.service.agent;

import com.agentedu.service.AiModelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentSchedulerService {

    private final AiModelClient aiModelClient;

    private final DiagnosisAgentService diagnosisAgentService;

    private final ExplanationAgentService explanationAgentService;

    private final RecommendAgentService recommendAgentService;

    private final EvaluationAgentService evaluationAgentService;

    /**
     * 统一调度多智能体。本阶段只调用一次Mock模型，再保留各Agent的结构化处理入口。
     */
    public AgentFeedbackResult generateFeedback(CodeContext context) {
        AgentFeedbackResult result = aiModelClient.chat(context);
        result.setDiagnosis(diagnosisAgentService.polish(result.getDiagnosis()));
        result.setExplanation(explanationAgentService.polish(result.getExplanation()));
        result.setRecommendProblems(recommendAgentService.polish(result.getRecommendProblems()));
        result.setEvaluation(evaluationAgentService.polish(result.getEvaluation()));
        result.setRagUsed(Boolean.TRUE.equals(context.getRagUsed()));
        result.setEvidenceChunkIds(context.getEvidenceChunkIds());
        result.setEvidenceSummary(context.getEvidenceSummary());
        return result;
    }
}
