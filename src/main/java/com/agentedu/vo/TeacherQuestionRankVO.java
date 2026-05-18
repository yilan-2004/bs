package com.agentedu.vo;

import lombok.Data;

@Data
public class TeacherQuestionRankVO {

    private Long problemId;

    private String problemTitle;

    private String bankName;

    private String subjectName;

    private Long submitCount;

    private Long acceptedCount;

    private Integer acceptedRate;
}
