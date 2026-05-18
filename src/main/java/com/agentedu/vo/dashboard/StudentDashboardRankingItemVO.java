package com.agentedu.vo.dashboard;

import lombok.Data;

@Data
public class StudentDashboardRankingItemVO {

    private Integer rank;

    private String studentName;

    private Long score;

    private Long submitCount;

    private Integer accuracyRate;

    private Boolean isMe;
}
