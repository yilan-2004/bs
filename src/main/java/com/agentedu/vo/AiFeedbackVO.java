package com.agentedu.vo;

import lombok.Data;

@Data
public class AiFeedbackVO {

    private Long id;

    private Long submitId;

    private Long problemId;

    private String problemTitle;

    private String judgeStatus;

    private String errorType;

    private String diagnosis;

    private String explanation;

    private String suggestion;

    private String evaluation;

    private String relatedKnowledge;

    private String nextPracticeAdvice;

    private Integer score;

    private String recommendProblems;

    private Boolean fromCache;

    private Boolean cacheHit;

    private Long cacheId;

    private Integer reuseCount;

    private String aiModel;

    private Boolean ragUsed;

    private String evidenceChunkIds;

    private String evidenceSummary;
}
