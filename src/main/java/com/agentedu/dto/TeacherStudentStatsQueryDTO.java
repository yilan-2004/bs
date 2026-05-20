package com.agentedu.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherStudentStatsQueryDTO {

    private Long page = 1L;

    private Long pageSize = 10L;

    private Long bankId;

    private Long problemId;

    private String keyword;

    private String activeStatus;

    private BigDecimal minAccuracy;

    private BigDecimal maxAccuracy;
}
