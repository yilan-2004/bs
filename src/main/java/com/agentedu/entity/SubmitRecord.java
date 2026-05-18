package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submit_record")
public class SubmitRecord {

    private Long id;

    private Long userId;

    private Long problemId;

    private String language;

    private String code;

    private String judgeStatus;

    private Integer passCount;

    private Integer totalCount;

    private Long runTime;

    private String errorMessage;

    private String outputResult;

    private Integer score;

    private Integer needAiFeedback;

    private String codeHash;

    private String errorFingerprint;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
