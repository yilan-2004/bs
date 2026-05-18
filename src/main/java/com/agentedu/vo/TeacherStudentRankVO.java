package com.agentedu.vo;

import lombok.Data;

@Data
public class TeacherStudentRankVO {

    private Long userId;

    private String username;

    private String realName;

    private Long submitCount;

    private Long acceptedCount;

    private Integer acceptedRate;

    private Long aiFeedbackCount;

    private Long score;
}
