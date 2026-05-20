package com.agentedu.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherStudentStatsVO {

    private Long studentId;

    private String studentName;

    private String username;

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;

    private BigDecimal accuracyRate;

    private Long aiFeedbackCount;

    private Long cacheHitCount;

    private LocalDateTime lastSubmitTime;

    private String activeStatus;
}
