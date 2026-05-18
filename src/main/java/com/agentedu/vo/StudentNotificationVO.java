package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentNotificationVO {

    private Long id;

    private String type;

    private String title;

    private String content;

    private Long submitId;

    private Long problemId;

    private String targetPath;

    private Boolean unread;

    private LocalDateTime createTime;
}
