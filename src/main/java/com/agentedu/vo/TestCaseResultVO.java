package com.agentedu.vo;

import lombok.Data;

@Data
public class TestCaseResultVO {

    private Long testCaseId;

    private String inputData;

    private String expectedOutput;

    private String actualOutput;

    private String errorOutput;

    private String judgeStatus;

    private Long runTime;

    private Integer passFlag;
}
