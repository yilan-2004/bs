package com.agentedu.vo;

import lombok.Data;

@Data
public class StudentBankProgressVO {

    private Long bankId;

    private String bankName;

    private Long totalProblems;

    private Long completedProblems;

    private Long submitCount;

    private Long acceptedCount;

    private Integer progressRate;
}
