package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentAccuracyTrendVO {

    private String date;

    private Long submitCount;

    private Integer acceptedRate;
}
