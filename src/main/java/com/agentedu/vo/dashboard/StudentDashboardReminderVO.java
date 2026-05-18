package com.agentedu.vo.dashboard;

import lombok.Data;

@Data
public class StudentDashboardReminderVO {

    private String type;

    private String title;

    private String content;

    private String targetUrl;
}
