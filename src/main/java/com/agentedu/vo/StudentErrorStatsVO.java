package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentErrorStatsVO {

    private String errorType;

    private Long count;
}
