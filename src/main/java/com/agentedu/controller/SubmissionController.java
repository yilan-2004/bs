package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.dto.SubmissionSubmitDTO;
import com.agentedu.service.SubmitService;
import com.agentedu.vo.SubmitResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmitService submitService;

    /**
     * 多题型统一作答入口：编程题分发到原代码评测，选择题和填空题走轻量评测。
     */
    @PostMapping("/submit")
    public Result<SubmitResultVO> submit(@Valid @RequestBody SubmissionSubmitDTO dto) {
        return Result.success(submitService.submitAnswer(dto));
    }
}
