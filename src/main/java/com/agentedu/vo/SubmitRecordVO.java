package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmitRecordVO {

    private Long id;

    private Long userId;

    private Long problemId;

    private String problemTitle;

    private Long bankId;

    private String bankName;

    private String language;

    private String judgeStatus;

    private Integer passCount;

    private Integer totalCount;

    private Long runTime;

    private Integer needAiFeedback;

    private Boolean fromCache;

    private Boolean cacheHit;

    private String codeHash;

    private String errorFingerprint;

    private LocalDateTime createTime;
}
