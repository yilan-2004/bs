package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentCalendarDayVO {

    private String date;

    private Integer dayOfMonth;

    private Long submitCount;

    private Long acceptedCount;

    private Boolean active;

    private Boolean hasWrong;
}
