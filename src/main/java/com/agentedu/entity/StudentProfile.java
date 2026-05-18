package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_profile")
public class StudentProfile {

    private Long id;

    private Long userId;

    private Integer acceptedCount;

    private Integer submitCount;

    private String weakKnowledgeTags;

    private LocalDateTime lastUpdateTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
