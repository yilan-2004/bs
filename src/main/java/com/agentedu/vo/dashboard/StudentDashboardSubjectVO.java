package com.agentedu.vo.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class StudentDashboardSubjectVO {

    private Long subjectId;

    private String subjectName;

    private Long bankCount;

    private Long practicedQuestionCount;

    private Long acceptedCount;

    private Long wrongCount;

    private Integer accuracyRate;

    private List<String> weakTags;
}
