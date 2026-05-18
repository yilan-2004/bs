package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestCaseVO {

    private Long id;

    private Long problemId;

    private String inputData;

    private String expectedOutput;

    private Integer isSample;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
