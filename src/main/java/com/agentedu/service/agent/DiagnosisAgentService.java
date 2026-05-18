package com.agentedu.service.agent;

import org.springframework.stereotype.Service;

@Service
public class DiagnosisAgentService {

    /**
     * 第一版仅保留错因诊断Agent入口。
     */
    public String polish(String diagnosis) {
        return diagnosis;
    }
}
