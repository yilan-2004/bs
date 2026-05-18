package com.agentedu.service;

import com.agentedu.entity.AiErrorCache;
import com.agentedu.service.agent.AgentFeedbackResult;

public interface AiCacheService {

    /**
     * 按题目、评测状态和错误指纹查询公共错误缓存。
     */
    AiErrorCache findCache(Long problemId, String judgeStatus, String errorFingerprint);

    /**
     * 将新生成的AI反馈写入公共错误缓存。
     */
    AiErrorCache saveCache(Long problemId, String judgeStatus, String errorFingerprint, AgentFeedbackResult result);

    /**
     * 递增缓存复用次数。
     */
    void increaseReuseCount(AiErrorCache cache);
}
