package com.agentedu.vo;

import lombok.Data;

@Data
public class TeacherKnowledgeWeaknessVO {

    private String knowledgeTag;

    private Long submitCount;

    private Long wrongCount;

    private Integer weaknessRate;
}
