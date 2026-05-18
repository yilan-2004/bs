package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProblemDTO {

    private Long id;

    @NotBlank(message = "题目标题不能为空")
    private String title;

    @NotBlank(message = "题目描述不能为空")
    private String description;

    private String inputDescription;

    private String outputDescription;

    private String sampleInput;

    private String sampleOutput;

    private String difficulty;

    private String knowledgeTags;
}
