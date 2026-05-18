package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProblemUpdateDTO {

    @NotNull(message = "题目ID不能为空")
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

    private Long bankId;

    private Long subjectId;

    private String questionType;

    private String standardAnswer;

    private String scoringPoints;

    private Integer score;

    private List<QuestionOptionDTO> options;

    private Integer status;
}
