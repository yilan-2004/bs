package com.agentedu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.config.AiProperties;
import com.agentedu.entity.AiErrorCache;
import com.agentedu.entity.AiFeedback;
import com.agentedu.entity.Problem;
import com.agentedu.entity.SubmitCaseResult;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.AiFeedbackMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.SubmitCaseResultMapper;
import com.agentedu.mapper.SubmitRecordMapper;
import com.agentedu.service.AiCacheService;
import com.agentedu.service.AiFeedbackService;
import com.agentedu.service.CodeContextBuilder;
import com.agentedu.service.KnowledgeRetrievalService;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.agent.AgentSchedulerService;
import com.agentedu.service.agent.CodeContext;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.AiFeedbackVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiFeedbackServiceImpl extends ServiceImpl<AiFeedbackMapper, AiFeedback> implements AiFeedbackService {

    private static final int FROM_CACHE_NO = 0;

    private static final int FROM_CACHE_YES = 1;

    private final SubmitRecordMapper submitRecordMapper;

    private final ProblemMapper problemMapper;

    private final SubmitCaseResultMapper submitCaseResultMapper;

    private final CodeContextBuilder codeContextBuilder;

    private final AgentSchedulerService agentSchedulerService;

    private final AiCacheService aiCacheService;

    private final KnowledgeRetrievalService knowledgeRetrievalService;

    private final AiProperties aiProperties;

    /**
     * Generate feedback for the current student's own submit record.
     * Existing ai_feedback and ai_error_cache are checked before any model call.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiFeedbackVO generateOrGetFeedback(Long submitId) {
        RoleAuthUtils.requireStudent();
        long loginUserId = StpUtil.getLoginIdAsLong();

        SubmitRecord submitRecord = submitRecordMapper.selectById(submitId);
        if (submitRecord == null) {
            throw new BusinessException("提交记录不存在");
        }
        if (!Long.valueOf(loginUserId).equals(submitRecord.getUserId())) {
            throw new BusinessException("No permission to generate AI feedback for this submission");
        }

        log.info("AI feedback request submitId={}, userId={}, problemId={}, judgeStatus={}, errorFingerprint={}",
                submitId, loginUserId, submitRecord.getProblemId(), submitRecord.getJudgeStatus(),
                submitRecord.getErrorFingerprint());

        Problem problem = problemMapper.selectById(submitRecord.getProblemId());
        if (problem == null) {
            throw new BusinessException("Problem does not exist");
        }

        AiFeedback existed = getOne(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getSubmitId, submitId)
                .last("LIMIT 1"));
        if (existed != null) {
            log.info("AI feedback hit existing record submitId={}, userId={}, problemId={}, feedbackId={}, fromCache={}",
                    submitId, loginUserId, submitRecord.getProblemId(), existed.getId(), existed.getFromCache());
            return toVO(existed, submitRecord, problem, null);
        }

        AiFeedback feedback;
        AiErrorCache cacheForVO = null;
        if (JudgeStatusEnum.ACCEPTED.name().equals(submitRecord.getJudgeStatus())) {
            log.info("AI feedback accepted template submitId={}, userId={}, problemId={}, skipCache=true, callAi=false",
                    submitId, loginUserId, submitRecord.getProblemId());
            feedback = buildFeedbackEntity(submitRecord, AgentFeedbackResult.accepted(), FROM_CACHE_NO, null);
        } else {
            AiErrorCache cached = aiCacheService.findCache(
                    submitRecord.getProblemId(),
                    submitRecord.getJudgeStatus(),
                    submitRecord.getErrorFingerprint());
            if (cached != null) {
                aiCacheService.increaseReuseCount(cached);
                cacheForVO = cached;
                log.info("AI error cache hit submitId={}, userId={}, problemId={}, cacheId={}, reuseCount={}, callAi=false",
                        submitId, loginUserId, submitRecord.getProblemId(), cached.getId(), cached.getReuseCount());
                feedback = buildFeedbackEntityFromCache(submitRecord, cached);
            } else {
                log.info("AI error cache miss submitId={}, userId={}, problemId={}, errorFingerprint={}, callAi=true",
                        submitId, loginUserId, submitRecord.getProblemId(), submitRecord.getErrorFingerprint());
                SubmitCaseResult failedCaseResult = findFirstFailedCase(submitId);
                CodeContext context = codeContextBuilder.build(submitRecord, problem, failedCaseResult);
                knowledgeRetrievalService.enrichContext(problem, context, submitRecord.getJudgeStatus());
                log.info("RAG retrieval submitId={}, userId={}, problemId={}, ragUsed={}, evidenceChunkIds={}",
                        submitId, loginUserId, submitRecord.getProblemId(), context.getRagUsed(), context.getEvidenceChunkIds());
                AgentFeedbackResult feedbackResult = agentSchedulerService.generateFeedback(context);
                aiCacheService.saveCache(
                        submitRecord.getProblemId(),
                        submitRecord.getJudgeStatus(),
                        submitRecord.getErrorFingerprint(),
                        feedbackResult);
                feedback = buildFeedbackEntity(submitRecord, feedbackResult, FROM_CACHE_NO, null);
            }
        }

        save(feedback);
        log.info("AI feedback saved submitId={}, userId={}, problemId={}, feedbackId={}, fromCache={}, cacheId={}",
                submitId, loginUserId, submitRecord.getProblemId(), feedback.getId(),
                feedback.getFromCache(), feedback.getCacheId());
        return toVO(feedback, submitRecord, problem, cacheForVO);
    }

    /**
     * 查看已有AI反馈，不生成、不查缓存、不调用模型。
     */
    @Override
    public AiFeedbackVO getExistingFeedback(Long submitId) {
        SubmitRecord submitRecord = submitRecordMapper.selectById(submitId);
        if (submitRecord == null) {
            throw new BusinessException("提交记录不存在");
        }
        Problem problem = problemMapper.selectById(submitRecord.getProblemId());
        if (problem == null) {
            throw new BusinessException("题目不存在");
        }
        checkFeedbackReadPermission(submitRecord, problem);

        AiFeedback feedback = getOne(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getSubmitId, submitId)
                .last("LIMIT 1"));
        if (feedback == null) {
            throw new BusinessException("AI反馈不存在");
        }
        return toVO(feedback, submitRecord, problem, null);
    }

    private SubmitCaseResult findFirstFailedCase(Long submitId) {
        return submitCaseResultMapper.selectOne(new LambdaQueryWrapper<SubmitCaseResult>()
                .eq(SubmitCaseResult::getSubmitId, submitId)
                .eq(SubmitCaseResult::getPassFlag, 0)
                .orderByAsc(SubmitCaseResult::getId)
                .last("LIMIT 1"));
    }

    private void checkFeedbackReadPermission(SubmitRecord submitRecord, Problem problem) {
        if (RoleAuthUtils.isStudent()) {
            if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(submitRecord.getUserId())) {
                throw new BusinessException("无权查看该AI反馈");
            }
            return;
        }
        if (RoleAuthUtils.isTeacher()) {
            if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(problem.getCreatorId())) {
                throw new BusinessException("无权查看该AI反馈");
            }
            return;
        }
        throw new BusinessException("无权查看该AI反馈");
    }

    private AiFeedback buildFeedbackEntity(SubmitRecord submitRecord, AgentFeedbackResult result,
                                           Integer fromCache, Long cacheId) {
        AiFeedback feedback = new AiFeedback();
        feedback.setSubmitId(submitRecord.getId());
        feedback.setUserId(submitRecord.getUserId());
        feedback.setProblemId(submitRecord.getProblemId());
        feedback.setErrorType(result.getErrorType());
        feedback.setDiagnosis(result.getDiagnosis());
        feedback.setExplanation(result.getExplanation());
        feedback.setSuggestion(result.getSuggestion());
        feedback.setEvaluation(result.getEvaluation());
        feedback.setRelatedKnowledge(result.getRelatedKnowledge());
        feedback.setNextPracticeAdvice(result.getNextPracticeAdvice());
        feedback.setScore(result.getScore());
        feedback.setRecommendProblems(result.getRecommendProblems());
        feedback.setFromCache(fromCache);
        feedback.setCacheId(cacheId);
        feedback.setAiModel(getAiModelName());
        feedback.setRagUsed(Boolean.TRUE.equals(result.getRagUsed()) ? 1 : 0);
        feedback.setEvidenceChunkIds(result.getEvidenceChunkIds());
        feedback.setEvidenceSummary(result.getEvidenceSummary());
        return feedback;
    }

    private AiFeedback buildFeedbackEntityFromCache(SubmitRecord submitRecord, AiErrorCache cache) {
        AiFeedback feedback = new AiFeedback();
        feedback.setSubmitId(submitRecord.getId());
        feedback.setUserId(submitRecord.getUserId());
        feedback.setProblemId(submitRecord.getProblemId());
        feedback.setErrorType(cache.getErrorType());
        feedback.setDiagnosis(cache.getDiagnosis());
        feedback.setExplanation(cache.getExplanation());
        feedback.setSuggestion(cache.getSuggestion());
        feedback.setEvaluation(cache.getEvaluation());
        feedback.setRelatedKnowledge(cache.getRelatedKnowledge());
        feedback.setNextPracticeAdvice(cache.getNextPracticeAdvice());
        feedback.setScore(cache.getScore());
        feedback.setRecommendProblems("");
        feedback.setFromCache(FROM_CACHE_YES);
        feedback.setCacheId(cache.getId());
        feedback.setAiModel(getAiModelName());
        feedback.setRagUsed(0);
        return feedback;
    }

    private String getAiModelName() {
        if (Boolean.TRUE.equals(aiProperties.getEnabled())) {
            return aiProperties.getProvider() + ":" + aiProperties.getModel();
        }
        return "mock-ai";
    }

    private AiFeedbackVO toVO(AiFeedback feedback, SubmitRecord submitRecord, Problem problem, AiErrorCache cache) {
        AiFeedbackVO vo = new AiFeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        vo.setFromCache(Integer.valueOf(1).equals(feedback.getFromCache()));
        vo.setCacheHit(vo.getFromCache());
        vo.setRagUsed(Integer.valueOf(1).equals(feedback.getRagUsed()));
        vo.setProblemTitle(problem.getTitle());
        vo.setJudgeStatus(submitRecord.getJudgeStatus());
        if (cache != null) {
            vo.setReuseCount(cache.getReuseCount());
        }
        return vo;
    }
}
