package com.agentedu.vo.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentDashboardRecordVO {

    private Long submitId;

    private Long problemId;

    private String problemTitle;

    private String subjectName;

    private String bankName;

    private String questionType;

    private String judgeStatus;

    private Integer passCount;

    private Integer totalCount;

    private LocalDateTime createTime;

    private Boolean hasAiFeedback;
}
