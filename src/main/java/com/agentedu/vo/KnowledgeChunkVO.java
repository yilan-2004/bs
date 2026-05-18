package com.agentedu.vo;

import lombok.Data;

@Data
public class KnowledgeChunkVO {

    private Long id;

    private Long baseId;

    private Long documentId;

    private Long subjectId;

    private String documentTitle;

    private String knowledgeTags;

    private String chunkText;

    private Integer chunkOrder;

    private Integer score;
}
