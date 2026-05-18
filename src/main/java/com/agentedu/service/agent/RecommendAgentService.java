package com.agentedu.service.agent;

import org.springframework.stereotype.Service;

@Service
public class RecommendAgentService {

    /**
     * 第一版仅返回简单推荐说明，可为空。
     */
    public String polish(String recommendProblems) {
        return recommendProblems == null ? "" : recommendProblems;
    }
}
