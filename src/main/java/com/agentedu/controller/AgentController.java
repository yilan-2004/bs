package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.dto.AgentAskDTO;
import com.agentedu.service.StudentAgentService;
import com.agentedu.vo.AgentAskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent/student")
@RequiredArgsConstructor
public class AgentController {

    private final StudentAgentService studentAgentService;

    /**
     * 学生 AI 助教自由问答。前端不直连 DeepSeek，统一走后端 AI 客户端。
     */
    @PostMapping("/ask")
    public Result<AgentAskVO> ask(@RequestBody AgentAskDTO dto) {
        return Result.success(studentAgentService.ask(dto));
    }
}
