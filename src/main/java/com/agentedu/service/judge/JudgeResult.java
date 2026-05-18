package com.agentedu.service.judge;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JudgeResult {

    private String judgeStatus;

    private Integer passCount = 0;

    private Integer totalCount = 0;

    private Long runTime = 0L;

    private String errorMessage;

    private String outputResult;

    private Integer score;

    private String errorFingerprint;

    private Integer needAiFeedback = 0;

    private List<TestCaseJudgeResult> testCaseResults = new ArrayList<>();
}
