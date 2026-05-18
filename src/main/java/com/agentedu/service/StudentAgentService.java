package com.agentedu.service;

import com.agentedu.dto.AgentAskDTO;
import com.agentedu.vo.AgentAskVO;

public interface StudentAgentService {

    /**
     * 调用后端 AI 客户端生成学生自由问答回复。
     */
    AgentAskVO ask(AgentAskDTO dto);
}
