package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionOptionDTO {

    private Long id;

    @NotBlank(message = "选项标识不能为空")
    private String optionKey;

    @NotBlank(message = "选项内容不能为空")
    private String optionContent;

    private Integer isCorrect;

    private Integer sortOrder;
}
