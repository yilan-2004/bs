package com.agentedu.vo;

import lombok.Data;

@Data
public class QuestionOptionVO {

    private Long id;

    private Long problemId;

    private String optionKey;

    private String optionContent;

    /**
     * 学生端不返回该字段，避免泄露正确答案。
     */
    private Integer isCorrect;

    private Integer sortOrder;
}
