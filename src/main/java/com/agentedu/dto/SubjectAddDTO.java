package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubjectAddDTO {

    @NotBlank(message = "学科名称不能为空")
    private String name;

    private String description;

    private String icon;

    private Integer sortOrder;
}
