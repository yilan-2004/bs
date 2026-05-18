package com.agentedu.dto;

import lombok.Data;

@Data
public class SubmitQueryDTO {

    private Long problemId;

    private Long userId;

    private String judgeStatus;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
