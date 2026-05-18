package com.agentedu.dto;

import lombok.Data;

@Data
public class KnowledgeBaseQueryDTO {

    private String keyword;

    private Long subjectId;

    private Integer status;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
