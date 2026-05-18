package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionSubmitDTO {

    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    private String questionType;

    @NotBlank(message = "答案内容不能为空")
    private String answerContent;

    private String language;
}
