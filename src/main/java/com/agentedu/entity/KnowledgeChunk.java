package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    private Long id;

    private Long baseId;

    private Long documentId;

    private Long subjectId;

    private String documentTitle;

    private String knowledgeTags;

    private String chunkText;

    private Integer chunkOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
