package com.agentedu.service;

import com.agentedu.common.PageResult;
import com.agentedu.dto.TeacherStudentStatsQueryDTO;
import com.agentedu.vo.TeacherStudentProfileVO;
import com.agentedu.vo.TeacherStudentStatsOverviewVO;
import com.agentedu.vo.TeacherStudentStatsVO;

public interface TeacherStudentStatsService {

    TeacherStudentStatsOverviewVO overview();

    PageResult<TeacherStudentStatsVO> pageStats(TeacherStudentStatsQueryDTO queryDTO);

    TeacherStudentProfileVO profile(Long studentId);
}
