package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentKnowledgeMasteryVO {

    private String knowledgeTag;

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;

    private Integer masteryRate;
}
