package com.agentedu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestCaseDTO {

    private Long id;

    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    private String inputData;

    private String expectedOutput;

    private Integer isSample;

    private Integer sortOrder;
}
