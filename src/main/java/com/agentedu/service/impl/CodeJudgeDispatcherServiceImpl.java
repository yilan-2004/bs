package com.agentedu.service.impl;

import com.agentedu.entity.SubmitRecord;
import com.agentedu.exception.BusinessException;
import com.agentedu.service.CodeJudgeService;
import com.agentedu.service.judge.JudgeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Primary
@RequiredArgsConstructor
public class CodeJudgeDispatcherServiceImpl implements CodeJudgeService {

    private final PythonCodeJudgeServiceImpl pythonCodeJudgeService;

    private final JavaCodeJudgeServiceImpl javaCodeJudgeService;

    /**
     * 根据提交记录中的 language 分发到对应语言评测器，避免影响原有 Python 评测流程。
     */
    @Override
    public JudgeResult judge(SubmitRecord submitRecord) {
        String language = StringUtils.hasText(submitRecord.getLanguage())
                ? submitRecord.getLanguage().toLowerCase()
                : "";
        if ("python".equals(language) || "py".equals(language)) {
            return pythonCodeJudgeService.judge(submitRecord);
        }
        if ("java".equals(language)) {
            return javaCodeJudgeService.judge(submitRecord);
        }
        throw new BusinessException("当前语言暂不支持代码评测");
    }
}
