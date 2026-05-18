package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseVO {

    private Long id;

    private String name;

    private String description;

    private Long subjectId;

    private String subjectName;

    private Long creatorId;

    private Integer status;

    private Integer documentCount;

    private Integer chunkCount;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
