package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentTrendVO {

    private String date;

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;
}
