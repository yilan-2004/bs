package com.agentedu.dto;

import lombok.Data;

@Data
public class ProblemQueryDTO {

    private String title;

    private String difficulty;

    private String knowledgeTags;

    private Long bankId;

    private Long subjectId;

    private String questionType;

    private Integer status;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
