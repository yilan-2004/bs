package com.agentedu.vo;

import lombok.Data;

@Data
public class TeacherOverviewVO {

    private Long bankCount;

    private Long problemCount;

    private Long submitCount;

    private Integer acceptedRate;

    private Long aiFeedbackCount;

    private Long cacheHitCount;
}
