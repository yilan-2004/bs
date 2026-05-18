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
import com.agentedu.vo.dashboard.StudentDashboardCalendarDayVO;
import com.agentedu.vo.dashboard.StudentDashboardOverviewVO;
import com.agentedu.vo.dashboard.StudentDashboardRankingItemVO;
import com.agentedu.vo.dashboard.StudentDashboardRankingVO;
import com.agentedu.vo.dashboard.StudentDashboardRecordVO;
import com.agentedu.vo.dashboard.StudentDashboardReminderVO;
import com.agentedu.vo.dashboard.StudentDashboardSubjectVO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final SubmitRecordMapper submitRecordMapper;

    private final AiFeedbackMapper aiFeedbackMapper;

    private final ProblemMapper problemMapper;

    private final ProblemBankMapper problemBankMapper;

    private final SubjectMapper subjectMapper;

    private final UserMapper userMapper;

    /**
     * 汇总当前学生首页概览数据，只统计当前登录学生自己的提交和反馈。
     */
    public StudentDashboardOverviewVO getOverview() {
        Long userId = currentStudentId();
        List<SubmitRecord> records = listStudentSubmits(userId);
        long submitCount = records.size();
        long acceptedCount = records.stream().filter(this::isAccepted).count();
        long wrongCount = submitCount - acceptedCount;
        long aiFeedbackCount = countStudentFeedback(userId, null);
        long todaySubmitCount = records.stream()
                .filter(record -> record.getCreateTime() != null)
                .filter(record -> record.getCreateTime().toLocalDate().equals(LocalDate.now()))
                .count();
        long wrongQuestionCount = records.stream()
                .filter(record -> !isAccepted(record))
                .map(SubmitRecord::getProblemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .size();

        StudentDashboardOverviewVO vo = new StudentDashboardOverviewVO();
        vo.setSubmitCount(submitCount);
        vo.setAcceptedCount(acceptedCount);
        vo.setWrongCount(wrongCount);
        vo.setAccuracyRate(rate(acceptedCount, submitCount));
        vo.setAiFeedbackCount(aiFeedbackCount);
        vo.setWrongQuestionCount(wrongQuestionCount);
        vo.setTodaySubmitCount(todaySubmitCount);
        vo.setLatestSubmitTime(records.isEmpty() ? null : records.get(0).getCreateTime());
        return vo;
    }

    /**
     * 返回学科/题库入口卡片数据，便于学生从首页进入对应题库。
     */
    public List<StudentDashboardSubjectVO> getSubjects() {
        Long userId = currentStudentId();
        List<Subject> subjects = subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getStatus, 1)
                .orderByAsc(Subject::getSortOrder)
                .orderByAsc(Subject::getId));
        List<ProblemBank> banks = problemBankMapper.selectList(new LambdaQueryWrapper<ProblemBank>()
                .eq(ProblemBank::getStatus, 1));
        List<SubmitRecord> records = listStudentSubmits(userId);
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());

        return subjects.stream().map(subject -> {
            List<SubmitRecord> subjectRecords = records.stream()
                    .filter(record -> {
                        Problem problem = problemMap.get(record.getProblemId());
                        return problem != null && Objects.equals(problem.getSubjectId(), subject.getId());
                    })
                    .toList();
            long submitCount = subjectRecords.size();
            long acceptedCount = subjectRecords.stream().filter(this::isAccepted).count();
            long wrongCount = submitCount - acceptedCount;
            Set<Long> practicedProblems = subjectRecords.stream()
                    .map(SubmitRecord::getProblemId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            StudentDashboardSubjectVO vo = new StudentDashboardSubjectVO();
            vo.setSubjectId(subject.getId());
            vo.setSubjectName(subject.getName());
            vo.setBankCount(banks.stream().filter(bank -> Objects.equals(bank.getSubjectId(), subject.getId())).count());
            vo.setPracticedQuestionCount((long) practicedProblems.size());
            vo.setAcceptedCount(acceptedCount);
            vo.setWrongCount(wrongCount);
            vo.setAccuracyRate(rate(acceptedCount, submitCount));
            vo.setWeakTags(topWeakTags(subjectRecords, problemMap, 3));
            return vo;
        }).toList();
    }

    /**
     * 按月返回练习日历数据，包含每天提交、通过、错误和 AI 反馈数量。
     */
    public List<StudentDashboardCalendarDayVO> getCalendar(String month) {
        Long userId = currentStudentId();
        YearMonth targetMonth = parseMonth(month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .ge(SubmitRecord::getCreateTime, start)
                .lt(SubmitRecord::getCreateTime, end));
        List<AiFeedback> feedbacks = aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getUserId, userId)
                .ge(AiFeedback::getCreateTime, start)
                .lt(AiFeedback::getCreateTime, end));

        Map<LocalDate, long[]> submitStats = new HashMap<>();
        for (SubmitRecord record : records) {
            if (record.getCreateTime() == null) {
                continue;
            }
            long[] item = submitStats.computeIfAbsent(record.getCreateTime().toLocalDate(), key -> new long[3]);
            item[0]++;
            if (isAccepted(record)) {
                item[1]++;
            } else {
                item[2]++;
            }
        }
        Map<LocalDate, Long> feedbackStats = feedbacks.stream()
                .filter(feedback -> feedback.getCreateTime() != null)
                .collect(Collectors.groupingBy(feedback -> feedback.getCreateTime().toLocalDate(), Collectors.counting()));

        List<StudentDashboardCalendarDayVO> result = new ArrayList<>();
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            long[] stats = submitStats.getOrDefault(date, new long[3]);
            StudentDashboardCalendarDayVO vo = new StudentDashboardCalendarDayVO();
            vo.setDate(date.toString());
            vo.setSubmitCount(stats[0]);
            vo.setAcceptedCount(stats[1]);
            vo.setWrongCount(stats[2]);
            vo.setAiFeedbackCount(feedbackStats.getOrDefault(date, 0L));
            result.add(vo);
        }
        return result;
    }

    /**
     * 返回某一天的练习记录，学生只能查看自己当天提交。
     */
    public List<StudentDashboardRecordVO> getDayRecords(String date) {
        Long userId = currentStudentId();
        LocalDate targetDate = StringUtils.hasText(date) ? LocalDate.parse(date) : LocalDate.now();
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .ge(SubmitRecord::getCreateTime, targetDate.atStartOfDay())
                .lt(SubmitRecord::getCreateTime, targetDate.plusDays(1).atStartOfDay())
                .orderByDesc(SubmitRecord::getCreateTime));
        return toRecordVOList(userId, records);
    }

    /**
     * 根据真实提交、反馈和薄弱知识点生成首页提醒。
     */
    public List<StudentDashboardReminderVO> getReminders() {
        Long userId = currentStudentId();
        List<SubmitRecord> records = listStudentSubmits(userId);
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Set<Long> feedbackSubmitIds = loadFeedbackSubmitIds(userId);
        List<StudentDashboardReminderVO> result = new ArrayList<>();

        records.stream()
                .filter(record -> !isAccepted(record))
                .filter(record -> Objects.equals(record.getNeedAiFeedback(), 1))
                .filter(record -> !feedbackSubmitIds.contains(record.getId()))
                .findFirst()
                .ifPresent(record -> {
                    Problem problem = problemMap.get(record.getProblemId());
                    result.add(reminder("AI_FEEDBACK_PENDING", "有待生成 AI 诊断的错误提交",
                            (problem == null ? "最近一次错误提交" : problem.getTitle()) + " 还没有生成错因诊断。",
                            "/student/submissions"));
                });

        boolean practicedToday = records.stream()
                .anyMatch(record -> record.getCreateTime() != null
                        && record.getCreateTime().toLocalDate().equals(LocalDate.now()));
        if (!practicedToday) {
            result.add(reminder("TODAY_NOT_PRACTICED", "今日还没有练习记录",
                    "完成一道题目即可点亮今天的学习日历。", "/student/banks"));
        }

        List<String> weakTags = topWeakTags(records, problemMap, 1);
        if (!weakTags.isEmpty()) {
            String tag = weakTags.get(0);
            result.add(reminder("WEAK_KNOWLEDGE", "薄弱知识点提醒",
                    "最近在「" + tag + "」相关题目上错误较多，建议进行专项练习。",
                    "/student/banks?knowledgeTag=" + tag));
        }

        records.stream()
                .filter(record -> !isAccepted(record))
                .findFirst()
                .ifPresent(record -> {
                    Problem problem = problemMap.get(record.getProblemId());
                    result.add(reminder("LATEST_WRONG", "最近一次评测未通过",
                            (problem == null ? "题目" : problem.getTitle()) + " 仍需要复盘，可重新练习或生成 AI 诊断。",
                            "/student/problem/" + record.getProblemId()));
                });

        return result.stream().limit(8).toList();
    }

    /**
     * 返回最近 10 条作答记录，用于首页最近练习表格。
     */
    public List<StudentDashboardRecordVO> getRecentSubmissions() {
        Long userId = currentStudentId();
        List<SubmitRecord> records = submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .orderByDesc(SubmitRecord::getCreateTime)
                .last("LIMIT 10"));
        return toRecordVOList(userId, records);
    }

    /**
     * 首页排行榜只展示分数和概览，不暴露其他学生提交详情。
     */
    public StudentDashboardRankingVO getRanking(String range) {
        Long currentUserId = currentStudentId();
        LocalDateTime startTime = parseRankingStartTime(range);
        List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "STUDENT")
                .eq(User::getStatus, 1));
        LambdaQueryWrapper<SubmitRecord> submitWrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            submitWrapper.ge(SubmitRecord::getCreateTime, startTime);
        }
        List<SubmitRecord> records = submitRecordMapper.selectList(submitWrapper);
        LambdaQueryWrapper<AiFeedback> feedbackWrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            feedbackWrapper.ge(AiFeedback::getCreateTime, startTime);
        }
        List<AiFeedback> feedbacks = aiFeedbackMapper.selectList(feedbackWrapper);

        Map<Long, List<SubmitRecord>> submitMap = records.stream()
                .collect(Collectors.groupingBy(SubmitRecord::getUserId));
        Map<Long, Long> feedbackMap = feedbacks.stream()
                .collect(Collectors.groupingBy(AiFeedback::getUserId, Collectors.counting()));

        List<StudentDashboardRankingItemVO> ranked = students.stream().map(user -> {
                    List<SubmitRecord> own = submitMap.getOrDefault(user.getId(), List.of());
                    long submitCount = own.size();
                    long acceptedCount = own.stream().filter(this::isAccepted).count();
                    long aiFeedbackCount = feedbackMap.getOrDefault(user.getId(), 0L);
                    StudentDashboardRankingItemVO vo = new StudentDashboardRankingItemVO();
                    vo.setStudentName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
                    vo.setSubmitCount(submitCount);
                    vo.setAccuracyRate(rate(acceptedCount, submitCount));
                    vo.setScore(acceptedCount * 10 + aiFeedbackCount * 2 + submitCount);
                    vo.setIsMe(Objects.equals(user.getId(), currentUserId));
                    return vo;
                })
                .sorted(Comparator.comparing(StudentDashboardRankingItemVO::getScore).reversed())
                .toList();
        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }

        StudentDashboardRankingItemVO myItem = ranked.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsMe()))
                .findFirst()
                .orElse(null);
        StudentDashboardRankingVO vo = new StudentDashboardRankingVO();
        vo.setMyRank(myItem == null ? null : myItem.getRank());
        vo.setMyScore(myItem == null ? 0L : myItem.getScore());
        vo.setList(ranked.stream().limit(10).toList());
        return vo;
    }

    private Long currentStudentId() {
        RoleAuthUtils.requireStudent();
        return StpUtil.getLoginIdAsLong();
    }

    private List<SubmitRecord> listStudentSubmits(Long userId) {
        return submitRecordMapper.selectList(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getUserId, userId)
                .orderByDesc(SubmitRecord::getCreateTime));
    }

    private long countStudentFeedback(Long userId, Integer fromCache) {
        LambdaQueryWrapper<AiFeedback> wrapper = new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getUserId, userId);
        if (fromCache != null) {
            wrapper.eq(AiFeedback::getFromCache, fromCache);
        }
        return aiFeedbackMapper.selectCount(wrapper);
    }

    private List<StudentDashboardRecordVO> toRecordVOList(Long userId, List<SubmitRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Map<Long, Problem> problemMap = loadProblemMap(records.stream().map(SubmitRecord::getProblemId).distinct().toList());
        Map<Long, ProblemBank> bankMap = loadBankMap(problemMap.values().stream()
                .map(Problem::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Map<Long, Subject> subjectMap = loadSubjectMap(problemMap.values().stream()
                .map(Problem::getSubjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Set<Long> feedbackSubmitIds = loadFeedbackSubmitIds(userId);

        return records.stream().map(record -> {
            Problem problem = problemMap.get(record.getProblemId());
            ProblemBank bank = problem == null || problem.getBankId() == null ? null : bankMap.get(problem.getBankId());
            Subject subject = problem == null || problem.getSubjectId() == null ? null : subjectMap.get(problem.getSubjectId());
            StudentDashboardRecordVO vo = new StudentDashboardRecordVO();
            vo.setSubmitId(record.getId());
            vo.setProblemId(record.getProblemId());
            vo.setProblemTitle(problem == null ? null : problem.getTitle());
            vo.setSubjectName(subject == null ? null : subject.getName());
            vo.setBankName(bank == null ? null : bank.getName());
            vo.setQuestionType(problem == null ? null : problem.getQuestionType());
            vo.setJudgeStatus(record.getJudgeStatus());
            vo.setPassCount(record.getPassCount());
            vo.setTotalCount(record.getTotalCount());
            vo.setCreateTime(record.getCreateTime());
            vo.setHasAiFeedback(feedbackSubmitIds.contains(record.getId()));
            return vo;
        }).toList();
    }

    private Set<Long> loadFeedbackSubmitIds(Long userId) {
        return new HashSet<>(aiFeedbackMapper.selectList(new LambdaQueryWrapper<AiFeedback>()
                        .eq(AiFeedback::getUserId, userId)
                        .select(AiFeedback::getSubmitId))
                .stream()
                .map(AiFeedback::getSubmitId)
                .filter(Objects::nonNull)
                .toList());
    }

    private List<String> topWeakTags(List<SubmitRecord> records, Map<Long, Problem> problemMap, int limit) {
        Map<String, long[]> stats = new LinkedHashMap<>();
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
        return stats.entrySet().stream()
                .filter(entry -> entry.getValue()[1] > 0)
                .sorted(Comparator.<Map.Entry<String, long[]>>comparingLong(entry -> entry.getValue()[1]).reversed()
                        .thenComparingLong(entry -> entry.getValue()[0]))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    private StudentDashboardReminderVO reminder(String type, String title, String content, String targetUrl) {
        StudentDashboardReminderVO vo = new StudentDashboardReminderVO();
        vo.setType(type);
        vo.setTitle(title);
        vo.setContent(content);
        vo.setTargetUrl(targetUrl);
        return vo;
    }

    private boolean isAccepted(SubmitRecord record) {
        return JudgeStatusEnum.ACCEPTED.name().equals(record.getJudgeStatus())
                || JudgeStatusEnum.PARTIAL_ACCEPTED.name().equals(record.getJudgeStatus());
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

    private LocalDateTime parseRankingStartTime(String range) {
        String normalized = StringUtils.hasText(range) ? range.trim().toUpperCase() : "WEEK";
        return switch (normalized) {
            case "ALL" -> null;
            case "MONTH" -> LocalDate.now().minusDays(29).atStartOfDay();
            default -> LocalDate.now().minusDays(6).atStartOfDay();
        };
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
}
