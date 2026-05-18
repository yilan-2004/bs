package com.agentedu.dto;

import lombok.Data;

@Data
public class ProblemBankQueryDTO {

    private String keyword;

    private String difficulty;

    private String knowledgeTag;

    private Long subjectId;

    private Integer status;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
