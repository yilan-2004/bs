package com.agentedu.service;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.entity.AiFeedback;
import com.agentedu.entity.Problem;
import com.agentedu.entity.ProblemBank;
import com.agentedu.entity.Subject;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.entity.User;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.mapper.AiFeedbackMapper;
import com.agentedu.mapper.ProblemBankMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.SubjectMapper;
import com.agentedu.mapper.SubmitRecordMapper;
import com.agentedu.mapper.UserMapper;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.StudentCalendarDayVO;
import com.agentedu.vo.StudentAccuracyTrendVO;
import com.agentedu.vo.StudentBankProgressVO;
import com.agentedu.vo.StudentErrorStatsVO;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SubmitRecordMapper submitRecordMapper;

    private final ProblemMapper problemMapper;

    private final ProblemBankMapper problemBankMapper;

    private final AiFeedbackMapper aiFeedbackMapper;

    private final UserMapper userMapper;

    private final SubjectMapper subjectMapper;

    /**
     * 学生只能统计自己的提交、AI反馈与缓存命中数据。
     */
    public StudentOverviewVO getStudentOverview() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        List<SubmitRecord> records = listStudentSubmits(userId);

        long submitCount = records.size();
        long acceptedCount = records.stream().filter(this::isAccepted).count();
        long aiFeedbackCount = aiFeedbackMapper.selectCount(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getUserId, userId));
        long cacheHitCount = aiFeedbackMapper.selectCount(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getUserId, userId)
                .eq(AiFeedback::getFromCache, 1));

        StudentOverviewVO vo = new StudentOverviewVO();
        vo.setSubmitCount(submitCount);
        vo.setAcceptedCount(acceptedCount);
        vo.setWrongCount(submitCount - acceptedCount);
        vo.setAcceptedRate(rate(acceptedCount, submitCount));
        vo.setAiFeedbackCount(aiFeedbackCount);
        vo.setCacheHitCount(cacheHitCount);
        vo.setRecentSubmitTime(records.isEmpty() ? null : records.get(0).getCreateTime());
        return vo;
    }

    /**
     * 按题目的知识点聚合学生薄弱情况。
     */
    public List<StudentKnowledgeVO> getStudentKnowledgeStats() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        List<SubmitRecord> records = listStudentSubmits(userId);
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Map<String, long[]> stats = new HashMap<>();

        for (SubmitRecord record : records) {
            Problem problem = problemMap.get(record.getProblemId());
            for (String tag : splitTags(problem == null ? null : problem.getKnowledgeTags())) {
                long[] pair = stats.computeIfAbsent(tag, key -> new long[2]);
                pair[0]++;
                if (!isAccepted(record)) {
                    pair[1]++;
                }
            }
        }

        return stats.entrySet().stream()
                .map(entry -> {
                    StudentKnowledgeVO vo = new StudentKnowledgeVO();
                    vo.setKnowledgeTag(entry.getKey());
                    vo.setSubmitCount(entry.getValue()[0]);
                    vo.setWrongCount(entry.getValue()[1]);
                    return vo;
                })
                .sorted(Comparator.comparing(StudentKnowledgeVO::getWrongCount).reversed())
                .toList();
    }

    /**
     * 统计学生已经生成AI反馈的错误类型。
     */
    public List<StudentErrorStatsVO> getStudentErrorStats() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Long> stats = aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                        .eq(AiFeedback::getUserId, userId))
                .stream()
                .filter(item -> StringUtils.hasText(item.getErrorType()))
                .collect(LinkedHashMap::new,
                        (map, item) -> map.merge(item.getErrorType(), 1L, Long::sum),
                        Map::putAll);
        return stats.entrySet().stream()
                .map(entry -> {
                    StudentErrorStatsVO vo = new StudentErrorStatsVO();
                    vo.setErrorType(entry.getKey());
                    vo.setCount(entry.getValue());
                    return vo;
                })
                .toList();
    }

    /**
     * 返回学生最近提交记录，不暴露代码和隐藏测试用例。
     */
    public List<StudentRecentSubmitVO> getStudentRecentSubmits() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .orderByDesc(SubmitRecord::getCreateTime)
                .last("LIMIT 10"));
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Map<Long, ProblemBank> bankMap = loadBankMap(problemMap.values().stream()
                .map(Problem::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        return records.stream().map(record -> toRecentSubmitVO(record, problemMap, bankMap)).toList();
    }

    /**
     * 学习日历来自学生当月真实提交记录，用于展示每天练习活跃情况。
     */
    public List<StudentCalendarDayVO> getStudentCalendar(String month) {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        YearMonth targetMonth = parseMonth(month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .ge(SubmitRecord::getCreateTime, start)
                .lt(SubmitRecord::getCreateTime, end));
        Map<LocalDate, long[]> grouped = new HashMap<>();
        for (SubmitRecord record : records) {
            if (record.getCreateTime() == null) {
                continue;
            }
            long[] pair = grouped.computeIfAbsent(record.getCreateTime().toLocalDate(), key -> new long[2]);
            pair[0]++;
            if (isAccepted(record)) {
                pair[1]++;
            }
        }

        List<StudentCalendarDayVO> result = new ArrayList<>();
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            long[] pair = grouped.getOrDefault(date, new long[2]);
            StudentCalendarDayVO vo = new StudentCalendarDayVO();
            vo.setDate(date.toString());
            vo.setDayOfMonth(day);
            vo.setSubmitCount(pair[0]);
            vo.setAcceptedCount(pair[1]);
            vo.setActive(pair[0] > 0);
            vo.setHasWrong(pair[0] > pair[1]);
            result.add(vo);
        }
        return result;
    }

    /**
     * 首页消息与提醒由业务事件生成：待AI诊断、最近评测完成、最近AI反馈。
     */
    public List<StudentNotificationVO> getStudentNotifications() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        List<StudentNotificationVO> result = new ArrayList<>();

        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .orderByDesc(SubmitRecord::getCreateTime)
                .last("LIMIT 30"));
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Set<Long> feedbackSubmitIds = new HashSet<>(aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                        .eq(AiFeedback::getUserId, userId)
                        .select(AiFeedback::getSubmitId))
                .stream()
                .map(AiFeedback::getSubmitId)
                .filter(Objects::nonNull)
                .toList());

        records.stream()
                .filter(record -> !isAccepted(record))
                .filter(record -> Objects.equals(record.getNeedAiFeedback(), 1))
                .filter(record -> !feedbackSubmitIds.contains(record.getId()))
                .limit(5)
                .forEach(record -> result.add(toNotification(record, problemMap, "AI_DIAGNOSIS",
                        "待生成AI诊断", "这次提交未通过，可以生成错因诊断与修改建议。", true)));

        records.stream()
                .limit(5)
                .forEach(record -> result.add(toNotification(record, problemMap, "JUDGE_RESULT",
                        "评测结果已更新", "提交状态：" + record.getJudgeStatus(), false)));

        aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                        .eq(AiFeedback::getUserId, userId)
                        .orderByDesc(AiFeedback::getCreateTime)
                        .last("LIMIT 5"))
                .forEach(feedback -> {
                    StudentNotificationVO vo = new StudentNotificationVO();
                    vo.setId(feedback.getId());
                    vo.setType("AI_FEEDBACK");
                    vo.setTitle("AI诊断已生成");
                    vo.setContent(StringUtils.hasText(feedback.getErrorType())
                            ? "诊断类型：" + feedback.getErrorType()
                            : "新的AI学习反馈已生成。");
                    vo.setSubmitId(feedback.getSubmitId());
                    vo.setProblemId(feedback.getProblemId());
                    vo.setTargetPath("/student/submissions");
                    vo.setUnread(false);
                    vo.setCreateTime(feedback.getCreateTime());
                    result.add(vo);
                });

        return result.stream()
                .sorted(Comparator.comparing(StudentNotificationVO::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .toList();
    }

    /**
     * 排行榜使用真实学习行为计算积分：通过、提交、AI诊断均计入。
     */
    public List<StudentRankingVO> getStudentRanking() {
        RoleAuthUtils.requireStudent();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "STUDENT")
                .eq(User::getStatus, 1));
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>());
        List<AiFeedback> feedbacks = aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>());

        Map<Long, List<SubmitRecord>> submitMap = new HashMap<>();
        for (SubmitRecord record : records) {
            submitMap.computeIfAbsent(record.getUserId(), key -> new ArrayList<>()).add(record);
        }
        Map<Long, Long> feedbackMap = new HashMap<>();
        for (AiFeedback feedback : feedbacks) {
            feedbackMap.merge(feedback.getUserId(), 1L, Long::sum);
        }

        List<StudentRankingVO> rankings = students.stream().map(user -> {
                    List<SubmitRecord> ownRecords = submitMap.getOrDefault(user.getId(), List.of());
                    long submitCount = ownRecords.size();
                    long acceptedCount = ownRecords.stream().filter(this::isAccepted).count();
                    long aiFeedbackCount = feedbackMap.getOrDefault(user.getId(), 0L);
                    StudentRankingVO vo = new StudentRankingVO();
                    vo.setUserId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setRealName(user.getRealName());
                    vo.setSubmitCount(submitCount);
                    vo.setAcceptedCount(acceptedCount);
                    vo.setAiFeedbackCount(aiFeedbackCount);
                    vo.setAcceptedRate(rate(acceptedCount, submitCount));
                    vo.setScore(acceptedCount * 20 + submitCount * 2 + aiFeedbackCount * 3);
                    vo.setCurrentUser(Objects.equals(user.getId(), currentUserId));
                    return vo;
                })
                .sorted(Comparator.comparing(StudentRankingVO::getScore, Comparator.reverseOrder())
                        .thenComparing(StudentRankingVO::getAcceptedCount, Comparator.reverseOrder()))
                .toList();

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRankNo(i + 1);
        }
        return rankings.stream().limit(10).toList();
    }

    /**
     * 教师只能统计自己创建题目范围内的数据。
     */
    public TeacherOverviewVO getTeacherOverview() {
        RoleAuthUtils.requireTeacher();
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Problem> ownProblems = listTeacherProblems(teacherId);
        List<Long> problemIds = ownProblems.stream().map(Problem::getId).toList();
        long submitCount = countSubmits(problemIds);
        long acceptedCount = countAcceptedSubmits(problemIds);

        TeacherOverviewVO vo = new TeacherOverviewVO();
        vo.setBankCount(problemBankMapper.selectCount(new LambdaQueryWrapper<ProblemBank>()
                .eq(ProblemBank::getCreatorId, teacherId)));
        vo.setProblemCount((long) ownProblems.size());
        vo.setSubmitCount(submitCount);
        vo.setAcceptedRate(rate(acceptedCount, submitCount));
        vo.setAiFeedbackCount(countFeedback(problemIds, null));
        vo.setCacheHitCount(countFeedback(problemIds, 1));
        return vo;
    }

    /**
     * 题目通过率排行仅统计当前教师创建的题目。
     */
    public List<TeacherProblemStatsVO> getTeacherProblemStats() {
        RoleAuthUtils.requireTeacher();
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Problem> ownProblems = listTeacherProblems(teacherId);
        Map<Long, ProblemBank> bankMap = loadBankMap(ownProblems.stream()
                .map(Problem::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        return ownProblems.stream()
                .map(problem -> {
                    long submitCount = submitRecordMapper.selectCount(new LambdaQueryWrapper<SubmitRecord>()
                            .eq(SubmitRecord::getProblemId, problem.getId()));
                    long acceptedCount = submitRecordMapper.selectCount(new LambdaQueryWrapper<SubmitRecord>()
                            .eq(SubmitRecord::getProblemId, problem.getId())
                            .eq(SubmitRecord::getJudgeStatus, JudgeStatusEnum.ACCEPTED.name()));
                    TeacherProblemStatsVO vo = new TeacherProblemStatsVO();
                    vo.setProblemId(problem.getId());
                    vo.setProblemTitle(problem.getTitle());
                    ProblemBank bank = problem.getBankId() == null ? null : bankMap.get(problem.getBankId());
                    vo.setBankName(bank == null ? null : bank.getName());
                    vo.setSubmitCount(submitCount);
                    vo.setAcceptedCount(acceptedCount);
                    vo.setAcceptedRate(rate(acceptedCount, submitCount));
                    return vo;
                })
                .sorted(Comparator.comparing(TeacherProblemStatsVO::getSubmitCount).reversed())
                .toList();
    }

    /**
     * 教师常见错误类型仅来自自己题目下的AI反馈。
     */
    public List<TeacherErrorStatsVO> getTeacherErrorStats() {
        RoleAuthUtils.requireTeacher();
        List<Long> problemIds = getTeacherProblemIds();
        if (problemIds.isEmpty()) {
            return List.of();
        }
        Map<String, Long> stats = aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                        .in(AiFeedback::getProblemId, problemIds))
                .stream()
                .filter(item -> StringUtils.hasText(item.getErrorType()))
                .collect(LinkedHashMap::new,
                        (map, item) -> map.merge(item.getErrorType(), 1L, Long::sum),
                        Map::putAll);
        return stats.entrySet().stream()
                .map(entry -> {
                    TeacherErrorStatsVO vo = new TeacherErrorStatsVO();
                    vo.setErrorType(entry.getKey());
                    vo.setCount(entry.getValue());
                    return vo;
                })
                .toList();
    }

    /**
     * 学生最近 30 天练习趋势，用真实提交记录按天聚合。
     */
    public List<StudentTrendVO> getStudentTrend() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate startDate = LocalDate.now().minusDays(29);
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .ge(SubmitRecord::getCreateTime, startDate.atStartOfDay()));
        Map<LocalDate, long[]> grouped = new HashMap<>();
        for (SubmitRecord record : records) {
            if (record.getCreateTime() == null) {
                continue;
            }
            long[] pair = grouped.computeIfAbsent(record.getCreateTime().toLocalDate(), key -> new long[2]);
            pair[0]++;
            if (isAccepted(record)) {
                pair[1]++;
            }
        }
        List<StudentTrendVO> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            long[] pair = grouped.getOrDefault(date, new long[2]);
            StudentTrendVO vo = new StudentTrendVO();
            vo.setDate(date.toString());
            vo.setSubmitCount(pair[0]);
            vo.setAcceptedCount(pair[1]);
            vo.setWrongCount(pair[0] - pair[1]);
            result.add(vo);
        }
        return result;
    }

    /**
     * 学生最近 30 天正确率趋势。
     */
    public List<StudentAccuracyTrendVO> getStudentAccuracyTrend() {
        return getStudentTrend().stream().map(item -> {
            StudentAccuracyTrendVO vo = new StudentAccuracyTrendVO();
            vo.setDate(item.getDate());
            vo.setSubmitCount(item.getSubmitCount());
            vo.setAcceptedRate(rate(item.getAcceptedCount(), item.getSubmitCount()));
            return vo;
        }).toList();
    }

    /**
     * 学生知识点掌握度，按题目知识标签聚合。
     */
    public List<StudentKnowledgeMasteryVO> getStudentKnowledgeMastery() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        List<SubmitRecord> records = listStudentSubmits(userId);
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Map<String, long[]> stats = new HashMap<>();
        for (SubmitRecord record : records) {
            Problem problem = problemMap.get(record.getProblemId());
            for (String tag : splitTags(problem == null ? null : problem.getKnowledgeTags())) {
                long[] item = stats.computeIfAbsent(tag, key -> new long[2]);
                item[0]++;
                if (isAccepted(record)) {
                    item[1]++;
                }
            }
        }
        return stats.entrySet().stream().map(entry -> {
                    StudentKnowledgeMasteryVO vo = new StudentKnowledgeMasteryVO();
                    vo.setKnowledgeTag(entry.getKey());
                    vo.setSubmitCount(entry.getValue()[0]);
                    vo.setAcceptedCount(entry.getValue()[1]);
                    vo.setWrongCount(entry.getValue()[0] - entry.getValue()[1]);
                    vo.setMasteryRate(rate(entry.getValue()[1], entry.getValue()[0]));
                    return vo;
                })
                .sorted(Comparator.comparing(StudentKnowledgeMasteryVO::getMasteryRate))
                .limit(12)
                .toList();
    }

    /**
     * 学生题库完成进度，按题库下已通过题目数量计算。
     */
    public List<StudentBankProgressVO> getStudentBankProgress() {
        RoleAuthUtils.requireStudent();
        Long userId = StpUtil.getLoginIdAsLong();
        List<ProblemBank> banks = problemBankMapper.selectList(new LambdaQueryWrapper<ProblemBank>()
                .eq(ProblemBank::getStatus, 1)
                .orderByAsc(ProblemBank::getSortOrder)
                .orderByDesc(ProblemBank::getCreateTime));
        List<SubmitRecord> records = listStudentSubmits(userId);
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Map<Long, Long> submitCountMap = new HashMap<>();
        Map<Long, Long> acceptedCountMap = new HashMap<>();
        Map<Long, Set<Long>> acceptedProblemMap = new HashMap<>();
        for (SubmitRecord record : records) {
            Problem problem = problemMap.get(record.getProblemId());
            Long bankId = problem == null ? null : problem.getBankId();
            if (bankId == null) {
                continue;
            }
            submitCountMap.merge(bankId, 1L, Long::sum);
            if (isAccepted(record)) {
                acceptedCountMap.merge(bankId, 1L, Long::sum);
                acceptedProblemMap.computeIfAbsent(bankId, key -> new HashSet<>()).add(record.getProblemId());
            }
        }
        return banks.stream().map(bank -> {
                    long totalProblems = problemMapper.selectCount(new LambdaQueryWrapper<Problem>()
                            .eq(Problem::getBankId, bank.getId())
                            .eq(Problem::getStatus, 1));
                    long completedProblems = acceptedProblemMap.getOrDefault(bank.getId(), Set.of()).size();
                    StudentBankProgressVO vo = new StudentBankProgressVO();
                    vo.setBankId(bank.getId());
                    vo.setBankName(bank.getName());
                    vo.setTotalProblems(totalProblems);
                    vo.setCompletedProblems(completedProblems);
                    vo.setSubmitCount(submitCountMap.getOrDefault(bank.getId(), 0L));
                    vo.setAcceptedCount(acceptedCountMap.getOrDefault(bank.getId(), 0L));
                    vo.setProgressRate(rate(completedProblems, totalProblems));
                    return vo;
                })
                .filter(item -> item.getTotalProblems() > 0 || item.getSubmitCount() > 0)
                .limit(10)
                .toList();
    }

    /**
     * 教师题目正确率排行，仅统计当前教师创建的题目。
     */
    public List<TeacherQuestionRankVO> getTeacherQuestionRank() {
        RoleAuthUtils.requireTeacher();
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Problem> problems = listTeacherProblems(teacherId);
        Map<Long, ProblemBank> bankMap = loadBankMap(problems.stream()
                .map(Problem::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Map<Long, Subject> subjectMap = loadSubjectMap(problems.stream()
                .map(Problem::getSubjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        return problems.stream().map(problem -> {
                    long submitCount = submitRecordMapper.selectCount(new LambdaQueryWrapper<SubmitRecord>()
                            .eq(SubmitRecord::getProblemId, problem.getId()));
                    long acceptedCount = submitRecordMapper.selectCount(new LambdaQueryWrapper<SubmitRecord>()
                            .eq(SubmitRecord::getProblemId, problem.getId())
                            .eq(SubmitRecord::getJudgeStatus, JudgeStatusEnum.ACCEPTED.name()));
                    ProblemBank bank = problem.getBankId() == null ? null : bankMap.get(problem.getBankId());
                    Subject subject = problem.getSubjectId() == null ? null : subjectMap.get(problem.getSubjectId());
                    TeacherQuestionRankVO vo = new TeacherQuestionRankVO();
                    vo.setProblemId(problem.getId());
                    vo.setProblemTitle(problem.getTitle());
                    vo.setBankName(bank == null ? null : bank.getName());
                    vo.setSubjectName(subject == null ? null : subject.getName());
                    vo.setSubmitCount(submitCount);
                    vo.setAcceptedCount(acceptedCount);
                    vo.setAcceptedRate(rate(acceptedCount, submitCount));
                    return vo;
                })
                .sorted(Comparator.comparing(TeacherQuestionRankVO::getAcceptedRate)
                        .thenComparing(TeacherQuestionRankVO::getSubmitCount, Comparator.reverseOrder()))
                .limit(12)
                .toList();
    }

    /**
     * 教师视角下的薄弱知识点排行。
     */
    public List<TeacherKnowledgeWeaknessVO> getTeacherKnowledgeWeakness() {
        RoleAuthUtils.requireTeacher();
        List<Long> problemIds = getTeacherProblemIds();
        if (problemIds.isEmpty()) {
            return List.of();
        }
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .in(SubmitRecord::getProblemId, problemIds));
        Map<Long, Problem> problemMap = loadProblemMap(problemIds);
        Map<String, long[]> stats = new HashMap<>();
        for (SubmitRecord record : records) {
            Problem problem = problemMap.get(record.getProblemId());
            for (String tag : splitTags(problem == null ? null : problem.getKnowledgeTags())) {
                long[] item = stats.computeIfAbsent(tag, key -> new long[2]);
                item[0]++;
                if (!isAccepted(record)) {
                    item[1]++;
                }
            }
        }
        return stats.entrySet().stream().map(entry -> {
                    TeacherKnowledgeWeaknessVO vo = new TeacherKnowledgeWeaknessVO();
                    vo.setKnowledgeTag(entry.getKey());
                    vo.setSubmitCount(entry.getValue()[0]);
                    vo.setWrongCount(entry.getValue()[1]);
                    vo.setWeaknessRate(rate(entry.getValue()[1], entry.getValue()[0]));
                    return vo;
                })
                .sorted(Comparator.comparing(TeacherKnowledgeWeaknessVO::getWrongCount).reversed())
                .limit(12)
                .toList();
    }

    /**
     * 学生活跃度排行，只统计当前教师题目下的提交。
     */
    public List<TeacherStudentRankVO> getTeacherStudentRank() {
        RoleAuthUtils.requireTeacher();
        List<Long> problemIds = getTeacherProblemIds();
        if (problemIds.isEmpty()) {
            return List.of();
        }
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .in(SubmitRecord::getProblemId, problemIds));
        Map<Long, List<SubmitRecord>> byUser = new HashMap<>();
        for (SubmitRecord record : records) {
            byUser.computeIfAbsent(record.getUserId(), key -> new ArrayList<>()).add(record);
        }
        if (byUser.isEmpty()) {
            return List.of();
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(byUser.keySet()).stream()
                .collect(HashMap::new, (map, user) -> map.put(user.getId(), user), Map::putAll);
        Map<Long, Long> feedbackCountMap = aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                        .in(AiFeedback::getProblemId, problemIds))
                .stream()
                .collect(HashMap::new,
                        (map, feedback) -> map.merge(feedback.getUserId(), 1L, Long::sum),
                        Map::putAll);
        return byUser.entrySet().stream().map(entry -> {
                    long submitCount = entry.getValue().size();
                    long acceptedCount = entry.getValue().stream().filter(this::isAccepted).count();
                    User user = userMap.get(entry.getKey());
                    long aiCount = feedbackCountMap.getOrDefault(entry.getKey(), 0L);
                    TeacherStudentRankVO vo = new TeacherStudentRankVO();
                    vo.setUserId(entry.getKey());
                    vo.setUsername(user == null ? null : user.getUsername());
                    vo.setRealName(user == null ? null : user.getRealName());
                    vo.setSubmitCount(submitCount);
                    vo.setAcceptedCount(acceptedCount);
                    vo.setAcceptedRate(rate(acceptedCount, submitCount));
                    vo.setAiFeedbackCount(aiCount);
                    vo.setScore(acceptedCount * 20 + submitCount * 2 + aiCount * 3);
                    return vo;
                })
                .sorted(Comparator.comparing(TeacherStudentRankVO::getScore).reversed())
                .limit(10)
                .toList();
    }

    /**
     * 教师最近 30 天 AI 使用和缓存命中趋势。
     */
    public List<TeacherAiUsageVO> getTeacherAiUsage() {
        RoleAuthUtils.requireTeacher();
        List<Long> problemIds = getTeacherProblemIds();
        if (problemIds.isEmpty()) {
            return List.of();
        }
        LocalDate startDate = LocalDate.now().minusDays(29);
        List<AiFeedback> feedbacks = aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                .in(AiFeedback::getProblemId, problemIds)
                .ge(AiFeedback::getCreateTime, startDate.atStartOfDay()));
        Map<LocalDate, long[]> grouped = new HashMap<>();
        for (AiFeedback feedback : feedbacks) {
            if (feedback.getCreateTime() == null) {
                continue;
            }
            long[] pair = grouped.computeIfAbsent(feedback.getCreateTime().toLocalDate(), key -> new long[2]);
            pair[0]++;
            if (Objects.equals(feedback.getFromCache(), 1)) {
                pair[1]++;
            }
        }
        List<TeacherAiUsageVO> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            long[] pair = grouped.getOrDefault(date, new long[2]);
            TeacherAiUsageVO vo = new TeacherAiUsageVO();
            vo.setDate(date.toString());
            vo.setAiFeedbackCount(pair[0]);
            vo.setCacheHitCount(pair[1]);
            vo.setCacheHitRate(rate(pair[1], pair[0]));
            result.add(vo);
        }
        return result;
    }

    /**
     * 教师最近提交记录，只返回自己题目范围内的数据。
     */
    public List<StudentRecentSubmitVO> getTeacherRecentSubmissions() {
        RoleAuthUtils.requireTeacher();
        List<Long> problemIds = getTeacherProblemIds();
        if (problemIds.isEmpty()) {
            return List.of();
        }
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .in(SubmitRecord::getProblemId, problemIds)
                .orderByDesc(SubmitRecord::getCreateTime)
                .last("LIMIT 12"));
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Map<Long, ProblemBank> bankMap = loadBankMap(problemMap.values().stream()
                .map(Problem::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        return records.stream().map(record -> toRecentSubmitVO(record, problemMap, bankMap)).toList();
    }

    private List<SubmitRecord> listStudentSubmits(Long userId) {
        return submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .orderByDesc(SubmitRecord::getCreateTime));
    }

    private List<Problem> listTeacherProblems(Long teacherId) {
        return problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .eq(Problem::getCreatorId, teacherId));
    }

    private List<Long> getTeacherProblemIds() {
        return listTeacherProblems(StpUtil.getLoginIdAsLong()).stream().map(Problem::getId).toList();
    }

    private long countSubmits(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return 0L;
        }
        return submitRecordMapper.selectCount(new LambdaQueryWrapper<SubmitRecord>()
                .in(SubmitRecord::getProblemId, problemIds));
    }

    private long countAcceptedSubmits(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return 0L;
        }
        return submitRecordMapper.selectCount(new LambdaQueryWrapper<SubmitRecord>()
                .in(SubmitRecord::getProblemId, problemIds)
                .eq(SubmitRecord::getJudgeStatus, JudgeStatusEnum.ACCEPTED.name()));
    }

    private long countFeedback(List<Long> problemIds, Integer fromCache) {
        if (problemIds.isEmpty()) {
            return 0L;
        }
        LambdaQueryWrapper<AiFeedback> wrapper = new LambdaQueryWrapper<AiFeedback>()
                .in(AiFeedback::getProblemId, problemIds);
        if (fromCache != null) {
            wrapper.eq(AiFeedback::getFromCache, fromCache);
        }
        return aiFeedbackMapper.selectCount(wrapper);
    }

    private boolean isAccepted(SubmitRecord record) {
        return JudgeStatusEnum.ACCEPTED.name().equals(record.getJudgeStatus());
    }

    private Integer rate(long acceptedCount, long submitCount) {
        if (submitCount == 0) {
            return 0;
        }
        return Math.toIntExact(Math.round(acceptedCount * 100.0 / submitCount));
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of("未标注");
        }
        List<String> result = new ArrayList<>();
        for (String tag : tags.split(",")) {
            String normalized = tag.trim();
            if (StringUtils.hasText(normalized)) {
                result.add(normalized);
            }
        }
        return result.isEmpty() ? List.of("未标注") : result;
    }

    private YearMonth parseMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        return YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private Map<Long, Problem> loadProblemMap(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        return problemMapper.selectBatchIds(problemIds)
                .stream()
                .collect(HashMap::new, (map, problem) -> map.put(problem.getId(), problem), Map::putAll);
    }

    private Map<Long, ProblemBank> loadBankMap(List<Long> bankIds) {
        if (bankIds.isEmpty()) {
            return Map.of();
        }
        return problemBankMapper.selectBatchIds(bankIds)
                .stream()
                .collect(HashMap::new, (map, bank) -> map.put(bank.getId(), bank), Map::putAll);
    }

    private Map<Long, Subject> loadSubjectMap(List<Long> subjectIds) {
        if (subjectIds.isEmpty()) {
            return Map.of();
        }
        return subjectMapper.selectBatchIds(subjectIds)
                .stream()
                .collect(HashMap::new, (map, subject) -> map.put(subject.getId(), subject), Map::putAll);
    }

    private StudentRecentSubmitVO toRecentSubmitVO(SubmitRecord record, Map<Long, Problem> problemMap,
                                                   Map<Long, ProblemBank> bankMap) {
        Problem problem = problemMap.get(record.getProblemId());
        ProblemBank bank = problem == null || problem.getBankId() == null ? null : bankMap.get(problem.getBankId());
        StudentRecentSubmitVO vo = new StudentRecentSubmitVO();
        vo.setSubmitId(record.getId());
        vo.setProblemTitle(problem == null ? null : problem.getTitle());
        vo.setBankName(bank == null ? null : bank.getName());
        vo.setJudgeStatus(record.getJudgeStatus());
        vo.setPassCount(record.getPassCount());
        vo.setTotalCount(record.getTotalCount());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private StudentNotificationVO toNotification(SubmitRecord record, Map<Long, Problem> problemMap, String type,
                                                 String title, String prefix, boolean unread) {
        Problem problem = problemMap.get(record.getProblemId());
        StudentNotificationVO vo = new StudentNotificationVO();
        vo.setId(record.getId());
        vo.setType(type);
        vo.setTitle(title);
        vo.setContent((problem == null ? "题目" : problem.getTitle()) + "：" + prefix);
        vo.setSubmitId(record.getId());
        vo.setProblemId(record.getProblemId());
        vo.setTargetPath("/student/submissions");
        vo.setUnread(unread);
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
