package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentAiAnalysisVO {

    private String summary;

    private String strengths;

    private String weaknesses;

    private String recommendations;

    private String nextWeekPlan;

    private String riskLevel;

    private Boolean generatedByAi;

    private String model;
}
