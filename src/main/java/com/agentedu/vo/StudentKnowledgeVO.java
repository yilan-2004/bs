package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentKnowledgeVO {

    private String knowledgeTag;

    private Long submitCount;

    private Long wrongCount;
}
