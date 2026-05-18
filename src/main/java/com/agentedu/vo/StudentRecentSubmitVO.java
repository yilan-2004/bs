package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentRecentSubmitVO {

    private Long submitId;

    private String problemTitle;

    private String bankName;

    private String judgeStatus;

    private Integer passCount;

    private Integer totalCount;

    private LocalDateTime createTime;
}
