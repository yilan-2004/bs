package com.agentedu.vo.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentDashboardOverviewVO {

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;

    private Integer accuracyRate;

    private Long aiFeedbackCount;

    private Long wrongQuestionCount;

    private Long todaySubmitCount;

    private LocalDateTime latestSubmitTime;
}
