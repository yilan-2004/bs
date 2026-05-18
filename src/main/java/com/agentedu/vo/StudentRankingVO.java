package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentRankingVO {

    private Long userId;

    private String username;

    private String realName;

    private Long submitCount;

    private Long acceptedCount;

    private Long aiFeedbackCount;

    private Integer acceptedRate;

    private Long score;

    private Integer rankNo;

    private Boolean currentUser;
}
