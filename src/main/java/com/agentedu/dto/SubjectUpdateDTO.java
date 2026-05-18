package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubjectUpdateDTO {

    @NotNull(message = "学科ID不能为空")
    private Long id;

    @NotBlank(message = "学科名称不能为空")
    private String name;

    private String description;

    private String icon;

    private Integer status;

    private Integer sortOrder;
}
