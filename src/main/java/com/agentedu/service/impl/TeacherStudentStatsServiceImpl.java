package com.agentedu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.common.PageResult;
import com.agentedu.dto.TeacherStudentStatsQueryDTO;
import com.agentedu.exception.BusinessException;
import com.agentedu.service.TeacherStudentStatsService;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.SubmitRecordVO;
import com.agentedu.vo.TeacherStudentProfileVO;
import com.agentedu.vo.TeacherStudentStatsOverviewVO;
import com.agentedu.vo.TeacherStudentStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherStudentStatsServiceImpl implements TeacherStudentStatsService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public TeacherStudentStatsOverviewVO overview() {
        RoleAuthUtils.requireTeacher();
        Long teacherId = StpUtil.getLoginIdAsLong();
        MapSqlParameterSource params = new MapSqlParameterSource("teacherId", teacherId);
        String sql = """
                SELECT
                  COUNT(DISTINCT sr.user_id) student_count,
                  COUNT(DISTINCT CASE WHEN sr.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN sr.user_id END) active_student_count,
                  COUNT(sr.id) submit_count,
                  SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) accepted_count,
                  COUNT(af.id) ai_feedback_count,
                  SUM(CASE WHEN af.from_cache = 1 THEN 1 ELSE 0 END) cache_hit_count
                FROM submit_record sr
                JOIN problem p ON p.id = sr.problem_id AND p.creator_id = :teacherId
                LEFT JOIN ai_feedback af ON af.submit_id = sr.id
                """;
        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
            long submitCount = rs.getLong("submit_count");
            long acceptedCount = rs.getLong("accepted_count");
            TeacherStudentStatsOverviewVO vo = new TeacherStudentStatsOverviewVO();
            vo.setStudentCount(rs.getLong("student_count"));
            vo.setActiveStudentCount(rs.getLong("active_student_count"));
            vo.setSubmitCount(submitCount);
            vo.setAverageAccuracyRate(rate(acceptedCount, submitCount));
            vo.setAiFeedbackCount(rs.getLong("ai_feedback_count"));
            vo.setCacheHitCount(rs.getLong("cache_hit_count"));
            return vo;
        });
    }

    @Override
    public PageResult<TeacherStudentStatsVO> pageStats(TeacherStudentStatsQueryDTO queryDTO) {
        RoleAuthUtils.requireTeacher();
        normalize(queryDTO);
        MapSqlParameterSource params = new MapSqlParameterSource("teacherId", StpUtil.getLoginIdAsLong());
        String where = buildWhere(queryDTO, params);
        String having = buildHaving(queryDTO, params);

        String groupedSql = """
                SELECT
                  sr.user_id student_id,
                  u.username username,
                  u.real_name real_name,
                  COUNT(sr.id) submit_count,
                  SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) accepted_count,
                  COUNT(sr.id) - SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) wrong_count,
                  ROUND(SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) * 100 / COUNT(sr.id), 2) accuracy_rate,
                  COUNT(af.id) ai_feedback_count,
                  SUM(CASE WHEN af.from_cache = 1 THEN 1 ELSE 0 END) cache_hit_count,
                  MAX(sr.create_time) last_submit_time
                FROM submit_record sr
                JOIN problem p ON p.id = sr.problem_id AND p.creator_id = :teacherId
                LEFT JOIN problem_bank pb ON pb.id = p.bank_id
                JOIN sys_user u ON u.id = sr.user_id
                LEFT JOIN ai_feedback af ON af.submit_id = sr.id
                """ + where + """
                GROUP BY sr.user_id, u.username, u.real_name
                """ + having;
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (" + groupedSql + ") t", params, Long.class);
        params.addValue("limit", queryDTO.getPageSize());
        params.addValue("offset", (queryDTO.getPage() - 1) * queryDTO.getPageSize());
        List<TeacherStudentStatsVO> records = jdbcTemplate.query(groupedSql + " ORDER BY last_submit_time DESC LIMIT :limit OFFSET :offset",
                params, (rs, rowNum) -> {
                    TeacherStudentStatsVO vo = new TeacherStudentStatsVO();
                    vo.setStudentId(rs.getLong("student_id"));
                    vo.setUsername(rs.getString("username"));
                    vo.setStudentName(maskName(rs.getString("real_name")));
                    vo.setSubmitCount(rs.getLong("submit_count"));
                    vo.setAcceptedCount(rs.getLong("accepted_count"));
                    vo.setWrongCount(rs.getLong("wrong_count"));
                    vo.setAccuracyRate(getBigDecimal(rs.getObject("accuracy_rate")));
                    vo.setAiFeedbackCount(rs.getLong("ai_feedback_count"));
                    vo.setCacheHitCount(rs.getLong("cache_hit_count"));
                    vo.setLastSubmitTime(toLocalDateTime(rs.getTimestamp("last_submit_time")));
                    vo.setActiveStatus(activeStatus(vo.getLastSubmitTime()));
                    return vo;
                });
        long pages = total == null || total == 0 ? 0 : (long) Math.ceil(total * 1.0 / queryDTO.getPageSize());
        return new PageResult<>(total == null ? 0L : total, pages, records);
    }

    @Override
    public TeacherStudentProfileVO profile(Long studentId) {
        RoleAuthUtils.requireTeacher();
        Long teacherId = StpUtil.getLoginIdAsLong();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("teacherId", teacherId)
                .addValue("studentId", studentId);
        String summarySql = """
                SELECT
                  u.id student_id,
                  u.username username,
                  u.real_name real_name,
                  COUNT(sr.id) submit_count,
                  SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) accepted_count,
                  COUNT(sr.id) - SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) wrong_count,
                  ROUND(SUM(CASE WHEN sr.judge_status IN ('ACCEPTED','PARTIAL_ACCEPTED') THEN 1 ELSE 0 END) * 100 / COUNT(sr.id), 2) accuracy_rate,
                  COUNT(af.id) ai_feedback_count,
                  SUM(CASE WHEN af.from_cache = 1 THEN 1 ELSE 0 END) cache_hit_count,
                  MAX(sr.create_time) last_submit_time
                FROM submit_record sr
                JOIN problem p ON p.id = sr.problem_id AND p.creator_id = :teacherId
                JOIN sys_user u ON u.id = sr.user_id
                LEFT JOIN ai_feedback af ON af.submit_id = sr.id
                WHERE sr.user_id = :studentId
                GROUP BY u.id, u.username, u.real_name
                """;
        List<TeacherStudentProfileVO> list = jdbcTemplate.query(summarySql, params, (rs, rowNum) -> {
            TeacherStudentProfileVO vo = new TeacherStudentProfileVO();
            vo.setStudentId(rs.getLong("student_id"));
            vo.setUsername(rs.getString("username"));
            vo.setStudentName(maskName(rs.getString("real_name")));
            vo.setSubmitCount(rs.getLong("submit_count"));
            vo.setAcceptedCount(rs.getLong("accepted_count"));
            vo.setWrongCount(rs.getLong("wrong_count"));
            vo.setAccuracyRate(getBigDecimal(rs.getObject("accuracy_rate")));
            vo.setAiFeedbackCount(rs.getLong("ai_feedback_count"));
            vo.setCacheHitCount(rs.getLong("cache_hit_count"));
            vo.setLastSubmitTime(toLocalDateTime(rs.getTimestamp("last_submit_time")));
            return vo;
        });
        if (list.isEmpty()) {
            throw new BusinessException("该学生暂无当前教师题目的学习数据");
        }
        TeacherStudentProfileVO vo = list.get(0);
        vo.setWeakKnowledgeTags(loadWeakTags(params));
        vo.setErrorTypeDistribution(loadErrorTypes(params));
        vo.setRecentSubmissions(loadRecentSubmissions(params));
        return vo;
    }

    private String buildWhere(TeacherStudentStatsQueryDTO queryDTO, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        if (queryDTO.getBankId() != null) {
            where.append(" AND p.bank_id = :bankId ");
            params.addValue("bankId", queryDTO.getBankId());
        }
        if (queryDTO.getProblemId() != null) {
            where.append(" AND p.id = :problemId ");
            params.addValue("problemId", queryDTO.getProblemId());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            where.append(" AND (u.username LIKE :keyword OR u.real_name LIKE :keyword OR CAST(u.id AS CHAR) LIKE :keyword) ");
            params.addValue("keyword", "%" + queryDTO.getKeyword().trim() + "%");
        }
        return where.toString();
    }

    private String buildHaving(TeacherStudentStatsQueryDTO queryDTO, MapSqlParameterSource params) {
        StringBuilder having = new StringBuilder(" HAVING 1 = 1 ");
        if (queryDTO.getMinAccuracy() != null) {
            having.append(" AND accuracy_rate >= :minAccuracy ");
            params.addValue("minAccuracy", queryDTO.getMinAccuracy());
        }
        if (queryDTO.getMaxAccuracy() != null) {
            having.append(" AND accuracy_rate <= :maxAccuracy ");
            params.addValue("maxAccuracy", queryDTO.getMaxAccuracy());
        }
        if ("ACTIVE".equals(queryDTO.getActiveStatus())) {
            having.append(" AND last_submit_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) ");
        } else if ("LOW_ACTIVITY".equals(queryDTO.getActiveStatus())) {
            having.append(" AND last_submit_time < DATE_SUB(NOW(), INTERVAL 7 DAY) AND last_submit_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) ");
        } else if ("NO_RECENT_ACTIVITY".equals(queryDTO.getActiveStatus())) {
            having.append(" AND last_submit_time < DATE_SUB(NOW(), INTERVAL 30 DAY) ");
        }
        return having.toString();
    }

    private List<String> loadWeakTags(MapSqlParameterSource params) {
        String sql = """
                SELECT p.knowledge_tags tags, COUNT(*) cnt
                FROM submit_record sr
                JOIN problem p ON p.id = sr.problem_id AND p.creator_id = :teacherId
                WHERE sr.user_id = :studentId AND sr.judge_status NOT IN ('ACCEPTED','PARTIAL_ACCEPTED')
                  AND p.knowledge_tags IS NOT NULL AND p.knowledge_tags <> ''
                GROUP BY p.knowledge_tags
                ORDER BY cnt DESC
                LIMIT 10
                """;
        Map<String, Long> counter = new LinkedHashMap<>();
        jdbcTemplate.query(sql, params, rs -> {
            for (String tag : rs.getString("tags").split("[,，;；]")) {
                String value = tag.trim();
                if (StringUtils.hasText(value)) {
                    counter.put(value, counter.getOrDefault(value, 0L) + rs.getLong("cnt"));
                }
            }
        });
        return counter.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Map<String, Object>> loadErrorTypes(MapSqlParameterSource params) {
        String sql = """
                SELECT COALESCE(af.error_type, sr.judge_status) error_type, COUNT(*) cnt
                FROM submit_record sr
                JOIN problem p ON p.id = sr.problem_id AND p.creator_id = :teacherId
                LEFT JOIN ai_feedback af ON af.submit_id = sr.id
                WHERE sr.user_id = :studentId AND sr.judge_status NOT IN ('ACCEPTED','PARTIAL_ACCEPTED')
                GROUP BY COALESCE(af.error_type, sr.judge_status)
                ORDER BY cnt DESC
                LIMIT 8
                """;
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("errorType", rs.getString("error_type"));
            item.put("count", rs.getLong("cnt"));
            return item;
        });
    }

    private List<SubmitRecordVO> loadRecentSubmissions(MapSqlParameterSource params) {
        String sql = """
                SELECT sr.id, sr.user_id, u.username, u.real_name, sr.problem_id, p.title problem_title,
                       p.bank_id, pb.name bank_name, sr.language, sr.judge_status, sr.pass_count,
                       sr.total_count, sr.run_time, sr.need_ai_feedback, sr.code_hash,
                       sr.error_fingerprint, sr.create_time,
                       CASE WHEN af.from_cache = 1 THEN 1 ELSE 0 END from_cache
                FROM submit_record sr
                JOIN problem p ON p.id = sr.problem_id AND p.creator_id = :teacherId
                LEFT JOIN problem_bank pb ON pb.id = p.bank_id
                JOIN sys_user u ON u.id = sr.user_id
                LEFT JOIN ai_feedback af ON af.submit_id = sr.id
                WHERE sr.user_id = :studentId
                ORDER BY sr.create_time DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            SubmitRecordVO vo = new SubmitRecordVO();
            vo.setId(rs.getLong("id"));
            vo.setUserId(rs.getLong("user_id"));
            vo.setUsername(rs.getString("username"));
            vo.setStudentName(maskName(rs.getString("real_name")));
            vo.setProblemId(rs.getLong("problem_id"));
            vo.setProblemTitle(rs.getString("problem_title"));
            vo.setBankId(rs.getLong("bank_id"));
            vo.setBankName(rs.getString("bank_name"));
            vo.setLanguage(rs.getString("language"));
            vo.setJudgeStatus(rs.getString("judge_status"));
            vo.setPassCount(rs.getInt("pass_count"));
            vo.setTotalCount(rs.getInt("total_count"));
            vo.setRunTime(rs.getLong("run_time"));
            vo.setNeedAiFeedback(rs.getInt("need_ai_feedback"));
            vo.setCodeHash(rs.getString("code_hash"));
            vo.setErrorFingerprint(rs.getString("error_fingerprint"));
            vo.setCreateTime(toLocalDateTime(rs.getTimestamp("create_time")));
            boolean fromCache = rs.getInt("from_cache") == 1;
            vo.setFromCache(fromCache);
            vo.setCacheHit(fromCache);
            return vo;
        });
    }

    private void normalize(TeacherStudentStatsQueryDTO queryDTO) {
        if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
            queryDTO.setPage(1L);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10L);
        }
        if (queryDTO.getPageSize() > 100) {
            queryDTO.setPageSize(100L);
        }
    }

    private String activeStatus(LocalDateTime lastSubmitTime) {
        if (lastSubmitTime == null) {
            return "NO_RECENT_ACTIVITY";
        }
        LocalDateTime now = LocalDateTime.now();
        if (!lastSubmitTime.isBefore(now.minusDays(7))) {
            return "ACTIVE";
        }
        if (!lastSubmitTime.isBefore(now.minusDays(30))) {
            return "LOW_ACTIVITY";
        }
        return "NO_RECENT_ACTIVITY";
    }

    private BigDecimal rate(long acceptedCount, long submitCount) {
        if (submitCount <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(acceptedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(submitCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String maskName(String realName) {
        if (!StringUtils.hasText(realName)) {
            return null;
        }
        String value = realName.trim();
        if (value.length() <= 1) {
            return value;
        }
        return value.substring(0, 1) + "*";
    }
}
