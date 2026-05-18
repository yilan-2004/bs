package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProblemBankAddDTO {

    @NotBlank(message = "题库名称不能为空")
    private String name;

    private String description;

    private String coverUrl;

    private String difficulty;

    private String knowledgeTags;

    private Long subjectId;

    private Integer sortOrder;
}
