package com.agentedu.dto;

import lombok.Data;

@Data
public class SubjectQueryDTO {

    private String keyword;

    private Integer status;

    private Long pageNum = 1L;

    private Long pageSize = 20L;
}
