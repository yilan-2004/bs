package com.agentedu.vo.dashboard;

import lombok.Data;

@Data
public class StudentDashboardCalendarDayVO {

    private String date;

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;

    private Long aiFeedbackCount;
}
