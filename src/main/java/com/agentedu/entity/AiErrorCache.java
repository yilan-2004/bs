package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_error_cache")
public class AiErrorCache {

    private Long id;

    private Long problemId;

    private String judgeStatus;

    private String errorFingerprint;

    private String errorType;

    private String diagnosis;

    private String explanation;

    private String suggestion;

    private String evaluation;

    private String relatedKnowledge;

    private String nextPracticeAdvice;

    private Integer score;

    private Integer reuseCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
