package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestCaseAddDTO {

    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    private String inputData;

    @NotBlank(message = "期望输出不能为空")
    private String expectedOutput;

    private Integer isSample;

    private Integer sortOrder;
}
