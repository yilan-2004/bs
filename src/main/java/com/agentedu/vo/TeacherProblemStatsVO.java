package com.agentedu.vo;

import lombok.Data;

@Data
public class TeacherProblemStatsVO {

    private Long problemId;

    private String problemTitle;

    private String bankName;

    private Long submitCount;

    private Long acceptedCount;

    private Integer acceptedRate;
}
