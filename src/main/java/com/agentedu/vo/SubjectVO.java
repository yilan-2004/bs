package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubjectVO {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private Integer status;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
