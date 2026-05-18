package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submit_case_result")
public class SubmitCaseResult {

    private Long id;

    private Long submitId;

    private Long testCaseId;

    private String inputData;

    private String expectedOutput;

    private String actualOutput;

    private String errorOutput;

    private String judgeStatus;

    private Long runTime;

    private Integer passFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
