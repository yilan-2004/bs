package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {

    private Long id;

    private Long baseId;

    private String title;

    private String content;

    private String knowledgeTags;

    private Integer status;

    private Integer chunkCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
