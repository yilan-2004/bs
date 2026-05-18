package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDocumentVO {

    private Long id;

    private Long baseId;

    private String baseName;

    private String title;

    private String content;

    private String knowledgeTags;

    private Integer status;

    private Integer chunkCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
