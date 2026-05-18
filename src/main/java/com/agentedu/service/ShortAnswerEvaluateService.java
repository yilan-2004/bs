package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.agent.AgentSchedulerService;
import com.agentedu.service.agent.CodeContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortAnswerEvaluateService {

    private final AgentSchedulerService agentSchedulerService;

    private final KnowledgeRetrievalService knowledgeRetrievalService;

    /**
     * Use the configured AI model to grade a short-answer response and return structured feedback.
     */
    public AgentFeedbackResult evaluate(Problem problem, Long submitId, String studentAnswer) {
        CodeContext context = new CodeContext();
        context.setSubmitId(submitId);
        context.setProblemId(problem.getId());
        context.setSubjectId(problem.getSubjectId());
        context.setProblemTitle(problem.getTitle());
        context.setProblemDescription(problem.getDescription());
        context.setKnowledgeTags(problem.getKnowledgeTags());
        context.setQuestionType("SHORT_ANSWER");
        context.setJudgeStatus("AI_GRADING");
        context.setStudentAnswer(studentAnswer);
        context.setCorrectAnswer(problem.getStandardAnswer());
        context.setScoringPoints(problem.getScoringPoints());
        context.setMaxScore(problem.getScore() == null ? 100 : problem.getScore());
        knowledgeRetrievalService.enrichContext(problem, context, null);
        return agentSchedulerService.generateFeedback(context);
    }
}
