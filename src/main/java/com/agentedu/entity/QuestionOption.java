package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_option")
public class QuestionOption {

    private Long id;

    private Long problemId;

    private String optionKey;

    private String optionContent;

    private Integer isCorrect;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
