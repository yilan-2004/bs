package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.TeacherStudentStatsQueryDTO;
import com.agentedu.service.TeacherStudentStatsService;
import com.agentedu.vo.TeacherStudentProfileVO;
import com.agentedu.vo.TeacherStudentStatsOverviewVO;
import com.agentedu.vo.TeacherStudentStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TeacherStudentStatsController {

    private final TeacherStudentStatsService teacherStudentStatsService;

    @GetMapping("/teacher/student-stats/overview")
    public Result<TeacherStudentStatsOverviewVO> overview() {
        return Result.success(teacherStudentStatsService.overview());
    }

    @GetMapping("/teacher/student-stats")
    public Result<PageResult<TeacherStudentStatsVO>> pageStats(TeacherStudentStatsQueryDTO queryDTO) {
        return Result.success(teacherStudentStatsService.pageStats(queryDTO));
    }

    @GetMapping("/teacher/students/{studentId}/profile")
    public Result<TeacherStudentProfileVO> profile(@PathVariable Long studentId) {
        return Result.success(teacherStudentStatsService.profile(studentId));
    }
}
