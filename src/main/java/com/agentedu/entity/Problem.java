package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("problem")
public class Problem {

    private Long id;

    private String title;

    private String description;

    private String inputDescription;

    private String outputDescription;

    private String sampleInput;

    private String sampleOutput;

    private String difficulty;

    private String knowledgeTags;

    private Long bankId;

    private Long subjectId;

    private String questionType;

    private String standardAnswer;

    private String scoringPoints;

    private Integer score;

    private Long creatorId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
