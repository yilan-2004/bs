package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmitDetailVO {

    private Long id;

    private Long userId;

    private String username;

    private String studentName;

    private Long problemId;

    private String problemTitle;

    private Long bankId;

    private String bankName;

    private String language;

    private String code;

    private String judgeStatus;

    private Integer passCount;

    private Integer totalCount;

    private Long runTime;

    private String errorMessage;

    private String outputResult;

    private Integer needAiFeedback;

    private Boolean fromCache;

    private Boolean cacheHit;

    private String codeHash;

    private String errorFingerprint;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
