package com.agentedu.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProblemVO {

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

    private String bankName;

    private Long subjectId;

    private String subjectName;

    private String questionType;

    private String standardAnswer;

    private String scoringPoints;

    private Integer score;

    private List<QuestionOptionVO> options;

    private Long creatorId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
