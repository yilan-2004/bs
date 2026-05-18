package com.agentedu.service.impl;

import com.agentedu.entity.AiErrorCache;
import com.agentedu.mapper.AiErrorCacheMapper;
import com.agentedu.service.AiCacheService;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiCacheServiceImpl extends ServiceImpl<AiErrorCacheMapper, AiErrorCache> implements AiCacheService {

    /**
     * errorFingerprint为空时不查询缓存。
     */
    @Override
    public AiErrorCache findCache(Long problemId, String judgeStatus, String errorFingerprint) {
        if (!StringUtils.hasText(errorFingerprint)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<AiErrorCache>()
                .eq(AiErrorCache::getProblemId, problemId)
                .eq(AiErrorCache::getJudgeStatus, judgeStatus)
                .eq(AiErrorCache::getErrorFingerprint, errorFingerprint)
                .last("LIMIT 1"));
    }

    /**
     * 将Mock AI生成的公共错误反馈写入缓存。
     */
    @Override
    public AiErrorCache saveCache(Long problemId, String judgeStatus, String errorFingerprint, AgentFeedbackResult result) {
        if (!StringUtils.hasText(errorFingerprint)) {
            return null;
        }
        AiErrorCache cache = new AiErrorCache();
        cache.setProblemId(problemId);
        cache.setJudgeStatus(judgeStatus);
        cache.setErrorFingerprint(errorFingerprint);
        cache.setErrorType(result.getErrorType());
        cache.setDiagnosis(result.getDiagnosis());
        cache.setExplanation(result.getExplanation());
        cache.setSuggestion(result.getSuggestion());
        cache.setEvaluation(result.getEvaluation());
        cache.setRelatedKnowledge(result.getRelatedKnowledge());
        cache.setNextPracticeAdvice(result.getNextPracticeAdvice());
        cache.setScore(result.getScore());
        cache.setReuseCount(0);
        save(cache);
        return cache;
    }

    /**
     * 缓存命中后复用次数加一。
     */
    @Override
    public void increaseReuseCount(AiErrorCache cache) {
        cache.setReuseCount(cache.getReuseCount() == null ? 1 : cache.getReuseCount() + 1);
        updateById(cache);
    }
}
