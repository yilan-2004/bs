package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeBaseUpdateDTO {

    @NotNull(message = "知识库ID不能为空")
    private Long id;

    @NotBlank(message = "知识库名称不能为空")
    private String name;

    private String description;

    private Long subjectId;

    private Integer sortOrder;
}
