package com.agentedu.service;

import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.agent.CodeContext;

public interface AiModelClient {

    /**
     * 根据代码上下文生成结构化反馈。本阶段使用Mock实现。
     */
    AgentFeedbackResult chat(CodeContext context);
}
