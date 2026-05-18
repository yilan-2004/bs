package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_feedback")
public class AiFeedback {

    private Long id;

    private Long submitId;

    private Long userId;

    private Long problemId;

    private String errorType;

    private String diagnosis;

    private String explanation;

    private String suggestion;

    private String evaluation;

    private String relatedKnowledge;

    private String nextPracticeAdvice;

    private Integer score;

    private String recommendProblems;

    private Integer fromCache;

    private Long cacheId;

    private String aiModel;

    private Integer ragUsed;

    private String evidenceChunkIds;

    private String evidenceSummary;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
