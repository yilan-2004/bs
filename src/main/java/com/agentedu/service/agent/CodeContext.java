package com.agentedu.service.agent;

import lombok.Data;

@Data
public class CodeContext {

    private Long submitId;

    private Long problemId;

    private Long subjectId;

    private String problemTitle;

    private String problemDescription;

    private String knowledgeTags;

    private String questionType;

    private String judgeStatus;

    private String failedInput;

    private String expectedOutput;

    private String actualOutput;

    private String errorMessage;

    private String keyCodeSnippet;

    private String studentAnswer;

    private String correctAnswer;

    private String scoringPoints;

    private Integer maxScore;

    private Boolean ragUsed;

    private String evidenceChunkIds;

    private String evidenceSummary;

    private String evidenceText;
}
