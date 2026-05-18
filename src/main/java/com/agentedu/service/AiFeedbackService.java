package com.agentedu.service;

import com.agentedu.vo.AiFeedbackVO;

public interface AiFeedbackService {

    /**
     * 生成或获取指定提交的AI诊断反馈。
     */
    AiFeedbackVO generateOrGetFeedback(Long submitId);

    /**
     * 查看已存在的AI反馈，不触发模型调用。
     */
    AiFeedbackVO getExistingFeedback(Long submitId);
}
