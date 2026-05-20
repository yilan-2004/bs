package com.agentedu.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SubmitQueryDTO {

    private Long problemId;

    private Long bankId;

    private Long userId;

    private Long studentId;

    private String judgeStatus;

    private Boolean hasAiFeedback;

    private Boolean fromCache;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long pageNum = 1L;

    private Long page;

    private Long pageSize = 10L;
}
