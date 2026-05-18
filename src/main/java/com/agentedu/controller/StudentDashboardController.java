package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.service.StudentDashboardService;
import com.agentedu.vo.dashboard.StudentDashboardCalendarDayVO;
import com.agentedu.vo.dashboard.StudentDashboardOverviewVO;
import com.agentedu.vo.dashboard.StudentDashboardRankingVO;
import com.agentedu.vo.dashboard.StudentDashboardRecordVO;
import com.agentedu.vo.dashboard.StudentDashboardReminderVO;
import com.agentedu.vo.dashboard.StudentDashboardSubjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard/student")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    @GetMapping("/overview")
    public Result<StudentDashboardOverviewVO> overview() {
        return Result.success(studentDashboardService.getOverview());
    }

    @GetMapping("/subjects")
    public Result<List<StudentDashboardSubjectVO>> subjects() {
        return Result.success(studentDashboardService.getSubjects());
    }

    @GetMapping("/calendar")
    public Result<List<StudentDashboardCalendarDayVO>> calendar(@RequestParam(required = false) String month) {
        return Result.success(studentDashboardService.getCalendar(month));
    }

    @GetMapping("/day-records")
    public Result<List<StudentDashboardRecordVO>> dayRecords(@RequestParam String date) {
        return Result.success(studentDashboardService.getDayRecords(date));
    }

    @GetMapping("/reminders")
    public Result<List<StudentDashboardReminderVO>> reminders() {
        return Result.success(studentDashboardService.getReminders());
    }

    @GetMapping("/recent-submissions")
    public Result<List<StudentDashboardRecordVO>> recentSubmissions() {
        return Result.success(studentDashboardService.getRecentSubmissions());
    }

    @GetMapping("/ranking")
    public Result<StudentDashboardRankingVO> ranking(@RequestParam(defaultValue = "WEEK") String range) {
        return Result.success(studentDashboardService.getRanking(range));
    }
}
