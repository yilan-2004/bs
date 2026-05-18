package com.agentedu.service;

import com.agentedu.entity.SubmitRecord;
import com.agentedu.service.judge.JudgeResult;

public interface CodeJudgeService {

    /**
     * 对提交记录执行代码评测，返回整体结果和每个测试用例结果。
     */
    JudgeResult judge(SubmitRecord submitRecord);
}
