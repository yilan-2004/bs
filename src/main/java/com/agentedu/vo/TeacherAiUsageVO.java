package com.agentedu.vo;

import lombok.Data;

@Data
public class TeacherAiUsageVO {

    private String date;

    private Long aiFeedbackCount;

    private Long cacheHitCount;

    private Integer cacheHitRate;
}
