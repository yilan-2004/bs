package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.service.ReportAiAnalysisService;
import com.agentedu.service.ReportService;
import com.agentedu.vo.StudentAiAnalysisVO;
import com.agentedu.vo.StudentErrorStatsVO;
import com.agentedu.vo.StudentCalendarDayVO;
import com.agentedu.vo.StudentAccuracyTrendVO;
import com.agentedu.vo.StudentBankProgressVO;
import com.agentedu.vo.StudentKnowledgeMasteryVO;
import com.agentedu.vo.StudentKnowledgeVO;
import com.agentedu.vo.StudentNotificationVO;
import com.agentedu.vo.StudentOverviewVO;
import com.agentedu.vo.StudentRankingVO;
import com.agentedu.vo.StudentRecentSubmitVO;
import com.agentedu.vo.StudentTrendVO;
import com.agentedu.vo.TeacherAiUsageVO;
import com.agentedu.vo.TeacherErrorStatsVO;
import com.agentedu.vo.TeacherKnowledgeWeaknessVO;
import com.agentedu.vo.TeacherOverviewVO;
import com.agentedu.vo.TeacherProblemStatsVO;
import com.agentedu.vo.TeacherQuestionRankVO;
import com.agentedu.vo.TeacherStudentRankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private final ReportAiAnalysisService reportAiAnalysisService;

    @GetMapping("/student/overview")
    public Result<StudentOverviewVO> studentOverview() {
        return Result.success(reportService.getStudentOverview());
    }

    @GetMapping("/student/knowledge")
    public Result<List<StudentKnowledgeVO>> studentKnowledge() {
        return Result.success(reportService.getStudentKnowledgeStats());
    }

    @GetMapping("/student/trend")
    public Result<List<StudentTrendVO>> studentTrend() {
        return Result.success(reportService.getStudentTrend());
    }

    @GetMapping("/student/accuracy-trend")
    public Result<List<StudentAccuracyTrendVO>> studentAccuracyTrend() {
        return Result.success(reportService.getStudentAccuracyTrend());
    }

    @GetMapping("/student/knowledge-mastery")
    public Result<List<StudentKnowledgeMasteryVO>> studentKnowledgeMastery() {
        return Result.success(reportService.getStudentKnowledgeMastery());
    }

    @GetMapping("/student/errors")
    public Result<List<StudentErrorStatsVO>> studentErrors() {
        return Result.success(reportService.getStudentErrorStats());
    }

    @GetMapping("/student/error-types")
    public Result<List<StudentErrorStatsVO>> studentErrorTypes() {
        return Result.success(reportService.getStudentErrorStats());
    }

    @GetMapping("/student/bank-progress")
    public Result<List<StudentBankProgressVO>> studentBankProgress() {
        return Result.success(reportService.getStudentBankProgress());
    }

    @GetMapping("/student/recent")
    public Result<List<StudentRecentSubmitVO>> studentRecent() {
        return Result.success(reportService.getStudentRecentSubmits());
    }

    @GetMapping("/student/recent-submissions")
    public Result<List<StudentRecentSubmitVO>> studentRecentSubmissions() {
        return Result.success(reportService.getStudentRecentSubmits());
    }

    @GetMapping("/student/calendar")
    public Result<List<StudentCalendarDayVO>> studentCalendar(@RequestParam(required = false) String month) {
        return Result.success(reportService.getStudentCalendar(month));
    }

    @GetMapping("/student/notifications")
    public Result<List<StudentNotificationVO>> studentNotifications() {
        return Result.success(reportService.getStudentNotifications());
    }

    @GetMapping("/student/ranking")
    public Result<List<StudentRankingVO>> studentRanking() {
        return Result.success(reportService.getStudentRanking());
    }

    @GetMapping("/student/ai-analysis")
    public Result<StudentAiAnalysisVO> studentAiAnalysis() {
        return Result.success(reportAiAnalysisService.analyzeStudentReport());
    }

    @GetMapping("/teacher/overview")
    public Result<TeacherOverviewVO> teacherOverview() {
        return Result.success(reportService.getTeacherOverview());
    }

    @GetMapping("/teacher/problem-stats")
    public Result<List<TeacherProblemStatsVO>> teacherProblemStats() {
        return Result.success(reportService.getTeacherProblemStats());
    }

    @GetMapping("/teacher/question-rank")
    public Result<List<TeacherQuestionRankVO>> teacherQuestionRank() {
        return Result.success(reportService.getTeacherQuestionRank());
    }

    @GetMapping("/teacher/knowledge-weakness")
    public Result<List<TeacherKnowledgeWeaknessVO>> teacherKnowledgeWeakness() {
        return Result.success(reportService.getTeacherKnowledgeWeakness());
    }

    @GetMapping("/teacher/error-stats")
    public Result<List<TeacherErrorStatsVO>> teacherErrorStats() {
        return Result.success(reportService.getTeacherErrorStats());
    }

    @GetMapping("/teacher/error-types")
    public Result<List<TeacherErrorStatsVO>> teacherErrorTypes() {
        return Result.success(reportService.getTeacherErrorStats());
    }

    @GetMapping("/teacher/student-rank")
    public Result<List<TeacherStudentRankVO>> teacherStudentRank() {
        return Result.success(reportService.getTeacherStudentRank());
    }

    @GetMapping("/teacher/ai-usage")
    public Result<List<TeacherAiUsageVO>> teacherAiUsage() {
        return Result.success(reportService.getTeacherAiUsage());
    }

    @GetMapping("/teacher/recent-submissions")
    public Result<List<StudentRecentSubmitVO>> teacherRecentSubmissions() {
        return Result.success(reportService.getTeacherRecentSubmissions());
    }
}
