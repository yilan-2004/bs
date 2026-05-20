package com.agentedu.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherStudentStatsOverviewVO {

    private Long studentCount;

    private Long activeStudentCount;

    private Long submitCount;

    private BigDecimal averageAccuracyRate;

    private Long aiFeedbackCount;

    private Long cacheHitCount;
}
