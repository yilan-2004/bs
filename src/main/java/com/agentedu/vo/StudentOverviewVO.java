package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentOverviewVO {

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;

    private Integer acceptedRate;

    private Long aiFeedbackCount;

    private Long cacheHitCount;

    private LocalDateTime recentSubmitTime;
}
