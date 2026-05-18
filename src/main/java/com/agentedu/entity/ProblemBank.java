package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("problem_bank")
public class ProblemBank {

    private Long id;

    private String name;

    private String description;

    private String coverUrl;

    private String difficulty;

    private String knowledgeTags;

    private Long subjectId;

    private Long creatorId;

    private Integer status;

    private Integer problemCount;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
