package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemBankVO {

    private Long id;

    private String name;

    private String description;

    private String coverUrl;

    private String difficulty;

    private String knowledgeTags;

    private Long subjectId;

    private String subjectName;

    private Long creatorId;

    private Integer status;

    private Integer problemCount;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
