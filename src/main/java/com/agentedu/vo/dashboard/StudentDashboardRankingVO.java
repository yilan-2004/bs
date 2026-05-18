package com.agentedu.vo.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class StudentDashboardRankingVO {

    private Integer myRank;

    private Long myScore;

    private List<StudentDashboardRankingItemVO> list;
}
