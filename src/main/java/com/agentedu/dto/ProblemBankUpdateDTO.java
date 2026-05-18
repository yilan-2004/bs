package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProblemBankUpdateDTO {

    @NotNull(message = "题库ID不能为空")
    private Long id;

    @NotBlank(message = "题库名称不能为空")
    private String name;

    private String description;

    private String coverUrl;

    private String difficulty;

    private String knowledgeTags;

    private Long subjectId;

    private Integer status;

    private Integer sortOrder;
}
