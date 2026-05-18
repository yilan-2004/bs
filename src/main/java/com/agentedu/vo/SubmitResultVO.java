package com.agentedu.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubmitResultVO {

    private Long submitId;

    private Long problemId;

    private String judgeStatus;

    private Integer passCount;

    private Integer totalCount;

    private Long runTime;

    private Integer needAiFeedback;

    private String codeHash;

    private String errorMessage;

    private String outputResult;

    private Integer score;

    private String errorFingerprint;

    private List<TestCaseResultVO> testCaseResults;

    private AiFeedbackVO aiFeedback;
}
