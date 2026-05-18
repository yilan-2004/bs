package com.agentedu.service.judge;

import lombok.Data;

@Data
public class TestCaseJudgeResult {

    private Long testCaseId;

    private String inputData;

    private String expectedOutput;

    private String actualOutput;

    private String errorOutput;

    private String judgeStatus;

    private Long runTime;

    private Integer passFlag;
}
