package com.agentedu.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TeacherStudentProfileVO {

    private Long studentId;

    private String studentName;

    private String username;

    private Long submitCount;

    private Long acceptedCount;

    private Long wrongCount;

    private BigDecimal accuracyRate;

    private Long aiFeedbackCount;

    private Long cacheHitCount;

    private LocalDateTime lastSubmitTime;

    private List<String> weakKnowledgeTags;

    private List<Map<String, Object>> errorTypeDistribution;

    private List<SubmitRecordVO> recentSubmissions;
}
