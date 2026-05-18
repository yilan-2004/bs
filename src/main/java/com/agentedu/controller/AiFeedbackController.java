package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.service.AiFeedbackService;
import com.agentedu.vo.AiFeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    /**
     * 生成或获取指定提交的AI诊断反馈。
     */
    @PostMapping("/feedback/{submitId}")
    public Result<AiFeedbackVO> feedback(@PathVariable Long submitId) {
        return Result.success(aiFeedbackService.generateOrGetFeedback(submitId));
    }

    /**
     * 查看已有AI反馈。学生只能看自己的提交，教师只能看自己题目下的反馈。
     */
    @GetMapping("/feedback/{submitId}")
    public Result<AiFeedbackVO> detail(@PathVariable Long submitId) {
        return Result.success(aiFeedbackService.getExistingFeedback(submitId));
    }
}
