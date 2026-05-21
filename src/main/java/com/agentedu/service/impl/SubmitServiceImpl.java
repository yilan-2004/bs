package com.agentedu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.common.PageResult;
import com.agentedu.config.AiProperties;
import com.agentedu.config.JudgeProperties;
import com.agentedu.dto.SubmitCodeDTO;
import com.agentedu.dto.SubmitQueryDTO;
import com.agentedu.dto.SubmissionSubmitDTO;
import com.agentedu.entity.AiErrorCache;
import com.agentedu.entity.AiFeedback;
import com.agentedu.entity.Problem;
import com.agentedu.entity.ProblemBank;
import com.agentedu.entity.SubmitCaseResult;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.entity.User;
import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.enums.QuestionTypeEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.AiFeedbackMapper;
import com.agentedu.mapper.ProblemBankMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.SubmitCaseResultMapper;
import com.agentedu.mapper.SubmitRecordMapper;
import com.agentedu.mapper.UserMapper;
import com.agentedu.service.ChoiceEvaluateService;
import com.agentedu.service.CodeJudgeService;
import com.agentedu.service.FillBlankEvaluateService;
import com.agentedu.service.AiCacheService;
import com.agentedu.service.AiFeedbackService;
import com.agentedu.service.ShortAnswerEvaluateService;
import com.agentedu.service.SubmitService;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.judge.JudgeResult;
import com.agentedu.service.judge.TestCaseJudgeResult;
import com.agentedu.utils.HashUtils;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.SubmitDetailVO;
import com.agentedu.vo.SubmitRecordVO;
import com.agentedu.vo.SubmitResultVO;
import com.agentedu.vo.TestCaseResultVO;
import com.agentedu.vo.AiFeedbackVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitServiceImpl extends ServiceImpl<SubmitRecordMapper, SubmitRecord> implements SubmitService {

    private static final int ENABLED_STATUS = 1;

    private static final int NEED_AI_FEEDBACK_NO = 0;

    private static final int NEED_AI_FEEDBACK_YES = 1;

    private final ProblemMapper problemMapper;

    private final ProblemBankMapper problemBankMapper;

    private final SubmitCaseResultMapper submitCaseResultMapper;

    private final AiFeedbackMapper aiFeedbackMapper;

    private final UserMapper userMapper;

    private final AiCacheService aiCacheService;

    private final AiFeedbackService aiFeedbackService;

    private final CodeJudgeService codeJudgeService;

    private final ChoiceEvaluateService choiceEvaluateService;

    private final FillBlankEvaluateService fillBlankEvaluateService;

    private final ShortAnswerEvaluateService shortAnswerEvaluateService;

    private final JudgeProperties judgeProperties;

    private final AiProperties aiProperties;

    /**
     * 学生提交Python代码：先保存JUDGING记录，再执行简化评测并回写结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submitCode(SubmitCodeDTO dto) {
        RoleAuthUtils.requireStudent();
        Problem problem = checkEnabledProblem(dto.getProblemId());
        checkLanguage(dto.getLanguage());
        checkCode(dto.getCode());

        SubmitRecord record = buildInitialSubmitRecord(dto, problem.getId());
        save(record);

        try {
            JudgeResult judgeResult = codeJudgeService.judge(record);
            saveCaseResults(record.getId(), judgeResult.getTestCaseResults());
            updateSubmitRecord(record, judgeResult);
            SubmitResultVO vo = toSubmitResultVO(record, judgeResult.getTestCaseResults());
            attachProgrammingAiReview(problem, record, vo);
            log.info("Judge finished submitId={}, userId={}, problemId={}, judgeStatus={}, passCount={}, totalCount={}, runTime={}, errorFingerprint={}",
                    record.getId(), record.getUserId(), record.getProblemId(), record.getJudgeStatus(),
                    record.getPassCount(), record.getTotalCount(), record.getRunTime(), record.getErrorFingerprint());
            return vo;
        } catch (BusinessException exception) {
            updateFailedSubmitRecord(record, JudgeStatusEnum.SYSTEM_ERROR.name(), exception.getMessage());
            log.warn("Judge business failure submitId={}, userId={}, problemId={}, message={}",
                    record.getId(), record.getUserId(), record.getProblemId(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            updateFailedSubmitRecord(record, JudgeStatusEnum.SYSTEM_ERROR.name(), "代码评测失败，请稍后重试");
            log.error("Judge system failure submitId={}, userId={}, problemId={}",
                    record.getId(), record.getUserId(), record.getProblemId(), exception);
            return toSubmitResultVO(record, List.of());
        }
    }

    /**
     * 多题型统一提交入口：编程题复用原评测流程，选择题和填空题走轻量评测。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submitAnswer(SubmissionSubmitDTO dto) {
        RoleAuthUtils.requireStudent();
        Problem problem = checkEnabledProblemForAnyQuestionType(dto.getProblemId());
        String questionType = StringUtils.hasText(problem.getQuestionType())
                ? problem.getQuestionType()
                : QuestionTypeEnum.PROGRAMMING.name();
        if (StringUtils.hasText(dto.getQuestionType()) && !questionType.equals(dto.getQuestionType())) {
            throw new BusinessException("提交题型与题目题型不一致");
        }

        if (QuestionTypeEnum.PROGRAMMING.name().equals(questionType)) {
            SubmitCodeDTO codeDTO = new SubmitCodeDTO();
            codeDTO.setProblemId(dto.getProblemId());
            codeDTO.setLanguage(StringUtils.hasText(dto.getLanguage()) ? dto.getLanguage() : "python");
            codeDTO.setCode(dto.getAnswerContent());
            return submitCode(codeDTO);
        }
        if (!QuestionTypeEnum.CHOICE.name().equals(questionType)
                && !QuestionTypeEnum.MULTI_CHOICE.name().equals(questionType)
                && !QuestionTypeEnum.FILL_BLANK.name().equals(questionType)
                && !QuestionTypeEnum.TRUE_FALSE.name().equals(questionType)
                && !QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType)) {
            throw new BusinessException("当前题型暂不支持作答评测");
        }

        SubmitRecord record = buildAnswerSubmitRecord(dto, problem, questionType);
        save(record);
        if (QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType)) {
            return evaluateShortAnswer(problem, record, dto.getAnswerContent());
        }
        JudgeResult judgeResult;
        if (QuestionTypeEnum.CHOICE.name().equals(questionType)) {
            judgeResult = choiceEvaluateService.evaluate(problem, dto.getAnswerContent());
        } else if (QuestionTypeEnum.MULTI_CHOICE.name().equals(questionType)) {
            judgeResult = choiceEvaluateService.evaluateMultiChoice(problem, dto.getAnswerContent());
        } else {
            judgeResult = fillBlankEvaluateService.evaluate(problem, dto.getAnswerContent());
        }
        saveCaseResults(record.getId(), judgeResult.getTestCaseResults());
        updateSubmitRecord(record, judgeResult);
        log.info("Answer evaluated submitId={}, userId={}, problemId={}, questionType={}, judgeStatus={}, errorFingerprint={}",
                record.getId(), record.getUserId(), record.getProblemId(), questionType,
                record.getJudgeStatus(), record.getErrorFingerprint());
        return toSubmitResultVO(record, judgeResult.getTestCaseResults());
    }

    private SubmitResultVO evaluateShortAnswer(Problem problem, SubmitRecord record, String answerContent) {
        long start = System.currentTimeMillis();
        AiFeedback cachedFeedback = tryReuseShortAnswerFeedback(problem, record);
        if (cachedFeedback != null) {
            JudgeResult cachedResult = buildShortAnswerResultFromFeedback(cachedFeedback, record.getJudgeStatus(),
                    record.getErrorFingerprint(), System.currentTimeMillis() - start, answerContent, problem);
            saveCaseResults(record.getId(), cachedResult.getTestCaseResults());
            updateSubmitRecord(record, cachedResult);
            SubmitResultVO vo = toSubmitResultVO(record, cachedResult.getTestCaseResults());
            vo.setAiFeedback(toAiFeedbackVO(cachedFeedback, record, problem));
            log.info("Short answer cache reused submitId={}, userId={}, problemId={}, judgeStatus={}, score={}, errorFingerprint={}",
                    record.getId(), record.getUserId(), record.getProblemId(), record.getJudgeStatus(),
                    record.getScore(), record.getErrorFingerprint());
            return vo;
        }

        try {
            AgentFeedbackResult feedbackResult = shortAnswerEvaluateService.evaluate(problem, record.getId(), answerContent);
            normalizeShortAnswerResult(feedbackResult);
            JudgeResult judgeResult = buildShortAnswerJudgeResult(problem, record, answerContent, feedbackResult,
                    System.currentTimeMillis() - start);
            saveCaseResults(record.getId(), judgeResult.getTestCaseResults());
            updateSubmitRecord(record, judgeResult);
            aiCacheService.saveCache(record.getProblemId(), record.getJudgeStatus(),
                    record.getErrorFingerprint(), feedbackResult);
            AiFeedback feedback = buildAiFeedback(record, feedbackResult, 0, null);
            aiFeedbackMapper.insert(feedback);
            SubmitResultVO vo = toSubmitResultVO(record, judgeResult.getTestCaseResults());
            vo.setAiFeedback(toAiFeedbackVO(feedback, record, problem));
            log.info("Short answer evaluated submitId={}, userId={}, problemId={}, judgeStatus={}, score={}, errorFingerprint={}",
                    record.getId(), record.getUserId(), record.getProblemId(), record.getJudgeStatus(),
                    record.getScore(), record.getErrorFingerprint());
            return vo;
        } catch (BusinessException exception) {
            JudgeResult failedResult = buildShortAnswerAiFailedResult(answerContent, problem, System.currentTimeMillis() - start);
            saveCaseResults(record.getId(), failedResult.getTestCaseResults());
            updateSubmitRecord(record, failedResult);
            log.warn("Short answer AI evaluate failed submitId={}, userId={}, problemId={}, message={}",
                    record.getId(), record.getUserId(), record.getProblemId(), exception.getMessage());
            return toSubmitResultVO(record, failedResult.getTestCaseResults());
        }
    }

    /**
     * 学生分页查看自己的提交记录。
     */
    @Override
    public PageResult<SubmitRecordVO> listMySubmissions(Long pageNum, Long pageSize) {
        RoleAuthUtils.requireStudent();
        Page<SubmitRecord> page = page(new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize)),
                new LambdaQueryWrapper<SubmitRecord>()
                        .eq(SubmitRecord::getUserId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(SubmitRecord::getCreateTime));
        return toRecordPage(page);
    }

    /**
     * 根据当前用户角色校验提交详情访问权限。
     */
    @Override
    public SubmitDetailVO getSubmitDetail(Long id) {
        SubmitRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("提交记录不存在");
        }
        if (RoleAuthUtils.isStudent()) {
            if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(record.getUserId())) {
                throw new BusinessException("无权查看该提交记录");
            }
        } else if (RoleAuthUtils.isTeacher()) {
            checkTeacherOwnsProblem(record.getProblemId());
        } else {
            throw new BusinessException("无权查看该提交记录");
        }
        return toDetailVO(record);
    }

    /**
     * 教师分页查看某道自己创建题目的提交记录。
     */
    @Override
    public PageResult<SubmitRecordVO> listByProblem(Long problemId, Long pageNum, Long pageSize) {
        RoleAuthUtils.requireTeacher();
        checkTeacherOwnsProblem(problemId);
        Page<SubmitRecord> page = page(new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize)),
                new LambdaQueryWrapper<SubmitRecord>()
                        .eq(SubmitRecord::getProblemId, problemId)
                        .orderByDesc(SubmitRecord::getCreateTime));
        return toRecordPage(page);
    }

    /**
     * 教师按条件查询提交记录，查询范围限定为自己创建的题目。
     */
    @Override
    public PageResult<SubmitRecordVO> listSubmissions(SubmitQueryDTO queryDTO) {
        RoleAuthUtils.requireTeacher();
        normalizeQuery(queryDTO);

        List<Long> ownProblemIds = getTeacherProblemIds();
        if (ownProblemIds.isEmpty()) {
            return new PageResult<>(0L, 0L, List.of());
        }
        if (queryDTO.getProblemId() != null && !ownProblemIds.contains(queryDTO.getProblemId())) {
            throw new BusinessException("无权查看该题目的提交记录");
        }

        if (queryDTO.getBankId() != null) {
            List<Long> bankProblemIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                            .eq(Problem::getCreatorId, StpUtil.getLoginIdAsLong())
                            .eq(Problem::getBankId, queryDTO.getBankId()))
                    .stream()
                    .map(Problem::getId)
                    .toList();
            if (bankProblemIds.isEmpty()) {
                return new PageResult<>(0L, 0L, List.of());
            }
            ownProblemIds = ownProblemIds.stream().filter(bankProblemIds::contains).toList();
            if (ownProblemIds.isEmpty()) {
                return new PageResult<>(0L, 0L, List.of());
            }
        }

        LambdaQueryWrapper<SubmitRecord> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getProblemId() != null) {
            wrapper.eq(SubmitRecord::getProblemId, queryDTO.getProblemId());
        } else {
            wrapper.in(SubmitRecord::getProblemId, ownProblemIds);
        }
        wrapper.eq(queryDTO.getUserId() != null, SubmitRecord::getUserId, queryDTO.getUserId());
        wrapper.eq(StringUtils.hasText(queryDTO.getJudgeStatus()), SubmitRecord::getJudgeStatus, queryDTO.getJudgeStatus());
        wrapper.ge(queryDTO.getStartTime() != null, SubmitRecord::getCreateTime, queryDTO.getStartTime());
        wrapper.le(queryDTO.getEndTime() != null, SubmitRecord::getCreateTime, queryDTO.getEndTime());
        if (Boolean.TRUE.equals(queryDTO.getHasAiFeedback())) {
            wrapper.exists("select 1 from ai_feedback af where af.submit_id = submit_record.id");
        } else if (Boolean.FALSE.equals(queryDTO.getHasAiFeedback())) {
            wrapper.notExists("select 1 from ai_feedback af where af.submit_id = submit_record.id");
        }
        if (Boolean.TRUE.equals(queryDTO.getFromCache())) {
            wrapper.exists("select 1 from ai_feedback af where af.submit_id = submit_record.id and af.from_cache = 1");
        } else if (Boolean.FALSE.equals(queryDTO.getFromCache())) {
            wrapper.notExists("select 1 from ai_feedback af where af.submit_id = submit_record.id and af.from_cache = 1");
        }
        wrapper.orderByDesc(SubmitRecord::getCreateTime);

        Page<SubmitRecord> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        return toRecordPage(page);
    }

    private SubmitRecord buildInitialSubmitRecord(SubmitCodeDTO dto, Long problemId) {
        String codeHash = HashUtils.md5(dto.getCode());
        SubmitRecord record = new SubmitRecord();
        record.setUserId(StpUtil.getLoginIdAsLong());
        record.setProblemId(problemId);
        record.setLanguage(normalizeLanguage(dto.getLanguage()));
        record.setCode(dto.getCode());
        record.setJudgeStatus(JudgeStatusEnum.JUDGING.name());
        record.setPassCount(0);
        record.setTotalCount(0);
        record.setRunTime(0L);
        record.setNeedAiFeedback(NEED_AI_FEEDBACK_NO);
        record.setCodeHash(codeHash);
        return record;
    }

    private SubmitRecord buildAnswerSubmitRecord(SubmissionSubmitDTO dto, Problem problem, String questionType) {
        String answer = dto.getAnswerContent() == null ? "" : dto.getAnswerContent();
        SubmitRecord record = new SubmitRecord();
        record.setUserId(StpUtil.getLoginIdAsLong());
        record.setProblemId(problem.getId());
        record.setLanguage(questionType.toLowerCase());
        record.setCode(answer);
        record.setJudgeStatus(JudgeStatusEnum.JUDGING.name());
        record.setPassCount(0);
        record.setTotalCount(0);
        record.setRunTime(0L);
        record.setNeedAiFeedback(NEED_AI_FEEDBACK_NO);
        record.setCodeHash(HashUtils.md5(QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType)
                ? normalizeShortAnswer(answer)
                : answer));
        return record;
    }

    private void updateSubmitRecord(SubmitRecord record, JudgeResult judgeResult) {
        record.setJudgeStatus(judgeResult.getJudgeStatus());
        record.setPassCount(judgeResult.getPassCount());
        record.setTotalCount(judgeResult.getTotalCount());
        record.setRunTime(judgeResult.getRunTime());
        record.setErrorMessage(judgeResult.getErrorMessage());
        record.setOutputResult(judgeResult.getOutputResult());
        record.setScore(judgeResult.getScore());
        record.setErrorFingerprint(judgeResult.getErrorFingerprint());
        record.setNeedAiFeedback(judgeResult.getNeedAiFeedback());
        updateById(record);
    }

    private AiFeedback tryReuseShortAnswerFeedback(Problem problem, SubmitRecord record) {
        SubmitRecord previous = getOne(new LambdaQueryWrapper<SubmitRecord>()
                .eq(SubmitRecord::getProblemId, problem.getId())
                .eq(SubmitRecord::getLanguage, QuestionTypeEnum.SHORT_ANSWER.name().toLowerCase())
                .eq(SubmitRecord::getCodeHash, record.getCodeHash())
                .isNotNull(SubmitRecord::getErrorFingerprint)
                .ne(SubmitRecord::getId, record.getId())
                .orderByDesc(SubmitRecord::getCreateTime)
                .last("LIMIT 1"));
        if (previous == null) {
            return null;
        }
        AiErrorCache cache = aiCacheService.findCache(previous.getProblemId(),
                previous.getJudgeStatus(), previous.getErrorFingerprint());
        if (cache == null) {
            return null;
        }
        aiCacheService.increaseReuseCount(cache);
        record.setJudgeStatus(previous.getJudgeStatus());
        record.setErrorFingerprint(previous.getErrorFingerprint());
        AgentFeedbackResult result = new AgentFeedbackResult();
        result.setErrorType(cache.getErrorType());
        result.setDiagnosis(cache.getDiagnosis());
        result.setExplanation(cache.getExplanation());
        result.setSuggestion(cache.getSuggestion());
        result.setEvaluation(cache.getEvaluation());
        result.setRelatedKnowledge(cache.getRelatedKnowledge());
        result.setNextPracticeAdvice(cache.getNextPracticeAdvice());
        result.setScore(cache.getScore());
        return buildAndSaveAiFeedback(record, result, 1, cache.getId());
    }

    private JudgeResult buildShortAnswerJudgeResult(Problem problem, SubmitRecord record, String answerContent,
                                                    AgentFeedbackResult feedbackResult, Long runTime) {
        String status = shortAnswerStatus(feedbackResult.getScore());
        String fingerprint = JudgeStatusEnum.ACCEPTED.name().equals(status) ? null : HashUtils.md5(
                String.valueOf(problem.getId())
                        + QuestionTypeEnum.SHORT_ANSWER.name()
                        + nullToEmpty(feedbackResult.getErrorType())
                        + nullToEmpty(feedbackResult.getRelatedKnowledge()));
        record.setJudgeStatus(status);
        record.setErrorFingerprint(fingerprint);

        JudgeResult result = new JudgeResult();
        result.setJudgeStatus(status);
        result.setPassCount(JudgeStatusEnum.WRONG_ANSWER.name().equals(status) ? 0 : 1);
        result.setTotalCount(1);
        result.setRunTime(runTime);
        result.setScore(feedbackResult.getScore());
        result.setErrorMessage(feedbackResult.getErrorType());
        result.setOutputResult("score=" + feedbackResult.getScore());
        result.setErrorFingerprint(fingerprint);
        result.setNeedAiFeedback(NEED_AI_FEEDBACK_NO);
        result.getTestCaseResults().add(buildShortAnswerCaseResult(problem, answerContent, status, runTime,
                result.getPassCount() == 1, feedbackResult.getErrorType()));
        return result;
    }

    private JudgeResult buildShortAnswerResultFromFeedback(AiFeedback feedback, String status, String fingerprint,
                                                           Long runTime, String answerContent, Problem problem) {
        JudgeResult result = new JudgeResult();
        result.setJudgeStatus(status);
        result.setPassCount(JudgeStatusEnum.WRONG_ANSWER.name().equals(status) ? 0 : 1);
        result.setTotalCount(1);
        result.setRunTime(runTime);
        result.setScore(feedback.getScore());
        result.setErrorMessage(feedback.getErrorType());
        result.setOutputResult("score=" + feedback.getScore());
        result.setErrorFingerprint(fingerprint);
        result.setNeedAiFeedback(NEED_AI_FEEDBACK_NO);
        result.getTestCaseResults().add(buildShortAnswerCaseResult(problem, answerContent, status, runTime,
                result.getPassCount() == 1, feedback.getErrorType()));
        return result;
    }

    private JudgeResult buildShortAnswerAiFailedResult(String answerContent, Problem problem, Long runTime) {
        JudgeResult result = new JudgeResult();
        result.setJudgeStatus(JudgeStatusEnum.AI_EVALUATE_FAILED.name());
        result.setPassCount(0);
        result.setTotalCount(1);
        result.setRunTime(runTime);
        result.setScore(0);
        result.setErrorMessage("AI service is temporarily unavailable, please try again later");
        result.setOutputResult("");
        result.setNeedAiFeedback(NEED_AI_FEEDBACK_YES);
        result.getTestCaseResults().add(buildShortAnswerCaseResult(problem, answerContent,
                JudgeStatusEnum.AI_EVALUATE_FAILED.name(), runTime, false, result.getErrorMessage()));
        return result;
    }

    private TestCaseJudgeResult buildShortAnswerCaseResult(Problem problem, String answerContent, String status,
                                                           Long runTime, boolean passed, String errorMessage) {
        TestCaseJudgeResult caseResult = new TestCaseJudgeResult();
        caseResult.setTestCaseId(0L);
        caseResult.setInputData(problem.getDescription());
        caseResult.setExpectedOutput(problem.getStandardAnswer());
        caseResult.setActualOutput(answerContent);
        caseResult.setErrorOutput(errorMessage);
        caseResult.setJudgeStatus(status);
        caseResult.setRunTime(runTime);
        caseResult.setPassFlag(passed ? 1 : 0);
        return caseResult;
    }

    private void normalizeShortAnswerResult(AgentFeedbackResult result) {
        if (result.getScore() == null) {
            result.setScore(0);
        }
        result.setScore(Math.max(0, Math.min(100, result.getScore())));
        if (!StringUtils.hasText(result.getErrorType())) {
            result.setErrorType("Short answer evaluation");
        }
        if (!StringUtils.hasText(result.getRelatedKnowledge())) {
            result.setRelatedKnowledge("Short answer knowledge points");
        }
    }

    private String shortAnswerStatus(Integer score) {
        int value = score == null ? 0 : score;
        if (value >= 95) {
            return JudgeStatusEnum.ACCEPTED.name();
        }
        if (value >= 60) {
            return JudgeStatusEnum.PARTIAL_ACCEPTED.name();
        }
        return JudgeStatusEnum.WRONG_ANSWER.name();
    }

    private AiFeedback buildAndSaveAiFeedback(SubmitRecord record, AgentFeedbackResult result, Integer fromCache, Long cacheId) {
        AiFeedback feedback = buildAiFeedback(record, result, fromCache, cacheId);
        aiFeedbackMapper.insert(feedback);
        return feedback;
    }

    private AiFeedback buildAiFeedback(SubmitRecord record, AgentFeedbackResult result, Integer fromCache, Long cacheId) {
        AiFeedback feedback = new AiFeedback();
        feedback.setSubmitId(record.getId());
        feedback.setUserId(record.getUserId());
        feedback.setProblemId(record.getProblemId());
        feedback.setErrorType(result.getErrorType());
        feedback.setDiagnosis(result.getDiagnosis());
        feedback.setExplanation(result.getExplanation());
        feedback.setSuggestion(result.getSuggestion());
        feedback.setEvaluation(result.getEvaluation());
        feedback.setRelatedKnowledge(result.getRelatedKnowledge());
        feedback.setNextPracticeAdvice(result.getNextPracticeAdvice());
        feedback.setScore(result.getScore());
        feedback.setRecommendProblems(result.getRecommendProblems());
        feedback.setFromCache(fromCache);
        feedback.setCacheId(cacheId);
        feedback.setAiModel(getAiModelName());
        feedback.setRagUsed(Boolean.TRUE.equals(result.getRagUsed()) ? 1 : 0);
        feedback.setEvidenceChunkIds(result.getEvidenceChunkIds());
        feedback.setEvidenceSummary(result.getEvidenceSummary());
        return feedback;
    }

    private AiFeedbackVO toAiFeedbackVO(AiFeedback feedback, SubmitRecord record, Problem problem) {
        AiFeedbackVO vo = new AiFeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        vo.setFromCache(Integer.valueOf(1).equals(feedback.getFromCache()));
        vo.setCacheHit(vo.getFromCache());
        vo.setRagUsed(Integer.valueOf(1).equals(feedback.getRagUsed()));
        vo.setProblemTitle(problem.getTitle());
        vo.setJudgeStatus(record.getJudgeStatus());
        return vo;
    }

    private String getAiModelName() {
        if (Boolean.TRUE.equals(aiProperties.getEnabled())) {
            return aiProperties.getProvider() + ":" + aiProperties.getModel();
        }
        return "mock-ai";
    }

    private String normalizeShortAnswer(String answer) {
        return answer == null ? "" : answer.trim().replaceAll("\\s+", " ");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void updateFailedSubmitRecord(SubmitRecord record, String judgeStatus, String errorMessage) {
        record.setJudgeStatus(judgeStatus);
        record.setErrorMessage(errorMessage);
        record.setNeedAiFeedback(NEED_AI_FEEDBACK_YES);
        updateById(record);
    }

    private void saveCaseResults(Long submitId, List<TestCaseJudgeResult> caseResults) {
        for (TestCaseJudgeResult caseResult : caseResults) {
            SubmitCaseResult entity = new SubmitCaseResult();
            entity.setSubmitId(submitId);
            entity.setTestCaseId(caseResult.getTestCaseId());
            entity.setInputData(caseResult.getInputData());
            entity.setExpectedOutput(caseResult.getExpectedOutput());
            entity.setActualOutput(caseResult.getActualOutput());
            entity.setErrorOutput(caseResult.getErrorOutput());
            entity.setJudgeStatus(caseResult.getJudgeStatus());
            entity.setRunTime(caseResult.getRunTime());
            entity.setPassFlag(caseResult.getPassFlag());
            submitCaseResultMapper.insert(entity);
        }
    }

    private Problem checkEnabledProblem(Long problemId) {
        Problem problem = checkEnabledProblemForAnyQuestionType(problemId);
        if (problem.getQuestionType() != null
                && !QuestionTypeEnum.PROGRAMMING.name().equals(problem.getQuestionType())) {
            throw new BusinessException("当前题型暂不支持代码评测");
        }
        return problem;
    }

    private Problem checkEnabledProblemForAnyQuestionType(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null || !Integer.valueOf(ENABLED_STATUS).equals(problem.getStatus())) {
            throw new BusinessException("题目不存在或已禁用");
        }
        checkProblemBankEnabled(problem.getBankId());
        return problem;
    }

    private Problem checkTeacherOwnsProblem(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException("题目不存在");
        }
        if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(problem.getCreatorId())) {
            throw new BusinessException("无权查看该题目的提交记录");
        }
        return problem;
    }

    private void checkProblemBankEnabled(Long bankId) {
        if (bankId == null) {
            return;
        }
        ProblemBank bank = problemBankMapper.selectById(bankId);
        if (bank == null || !Integer.valueOf(ENABLED_STATUS).equals(bank.getStatus())) {
            throw new BusinessException("题目不存在或已禁用");
        }
    }

    private List<Long> getTeacherProblemIds() {
        return problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                        .eq(Problem::getCreatorId, StpUtil.getLoginIdAsLong()))
                .stream()
                .map(Problem::getId)
                .toList();
    }

    private void checkLanguage(String language) {
        String normalized = normalizeLanguage(language);
        if (!"python".equals(normalized) && !"java".equals(normalized)) {
            throw new BusinessException("当前仅支持 Python 或 Java 代码提交");
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "python";
        }
        String normalized = language.trim().toLowerCase();
        return "py".equals(normalized) ? "python" : normalized;
    }

    private void checkCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("代码不能为空");
        }
        if (code.length() > judgeProperties.getMaxCodeLength()) {
            throw new BusinessException("代码长度不能超过50KB");
        }
    }

    private void normalizeQuery(SubmitQueryDTO queryDTO) {
        if (queryDTO.getPage() != null) {
            queryDTO.setPageNum(queryDTO.getPage());
        }
        if (queryDTO.getStudentId() != null && queryDTO.getUserId() == null) {
            queryDTO.setUserId(queryDTO.getStudentId());
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1L);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10L);
        }
        if (queryDTO.getPageSize() > 100) {
            queryDTO.setPageSize(100L);
        }
    }

    private long normalizePageNum(Long pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize, 100L);
    }

    private PageResult<SubmitRecordVO> toRecordPage(Page<SubmitRecord> page) {
        return new PageResult<>(page.getTotal(), page.getPages(), page.getRecords().stream().map(this::toRecordVO).toList());
    }

    private SubmitRecordVO toRecordVO(SubmitRecord record) {
        SubmitRecordVO vo = new SubmitRecordVO();
        BeanUtils.copyProperties(record, vo);
        fillSubmitDisplayFields(vo, record);
        return vo;
    }

    private SubmitDetailVO toDetailVO(SubmitRecord record) {
        SubmitDetailVO vo = new SubmitDetailVO();
        BeanUtils.copyProperties(record, vo);
        fillSubmitDisplayFields(vo, record);
        return vo;
    }

    private void fillSubmitDisplayFields(SubmitRecordVO vo, SubmitRecord record) {
        fillStudentDisplayFields(vo, record.getUserId());
        Problem problem = problemMapper.selectById(record.getProblemId());
        if (problem != null) {
            vo.setProblemTitle(problem.getTitle());
            vo.setBankId(problem.getBankId());
            vo.setBankName(getBankName(problem.getBankId()));
        }
        boolean fromCache = hasCacheFeedback(record.getId());
        vo.setFromCache(fromCache);
        vo.setCacheHit(fromCache);
    }

    private void fillSubmitDisplayFields(SubmitDetailVO vo, SubmitRecord record) {
        fillStudentDisplayFields(vo, record.getUserId());
        Problem problem = problemMapper.selectById(record.getProblemId());
        if (problem != null) {
            vo.setProblemTitle(problem.getTitle());
            vo.setBankId(problem.getBankId());
            vo.setBankName(getBankName(problem.getBankId()));
        }
        boolean fromCache = hasCacheFeedback(record.getId());
        vo.setFromCache(fromCache);
        vo.setCacheHit(fromCache);
    }

    private void fillStudentDisplayFields(SubmitRecordVO vo, Long userId) {
        if (userId == null) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        vo.setUsername(user.getUsername());
        vo.setStudentName(maskName(user.getRealName()));
    }

    private void fillStudentDisplayFields(SubmitDetailVO vo, Long userId) {
        if (userId == null) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        vo.setUsername(user.getUsername());
        vo.setStudentName(maskName(user.getRealName()));
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

    private boolean hasCacheFeedback(Long submitId) {
        AiFeedback feedback = aiFeedbackMapper.selectOne(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getSubmitId, submitId)
                .last("LIMIT 1"));
        return feedback != null && Integer.valueOf(1).equals(feedback.getFromCache());
    }

    private String getBankName(Long bankId) {
        if (bankId == null) {
            return null;
        }
        ProblemBank bank = problemBankMapper.selectById(bankId);
        return bank == null ? null : bank.getName();
    }

    private void attachProgrammingAiReview(Problem problem, SubmitRecord record, SubmitResultVO vo) {
        if (!QuestionTypeEnum.PROGRAMMING.name().equals(problem.getQuestionType()) && problem.getQuestionType() != null) {
            return;
        }
        try {
            AiFeedbackVO feedback = aiFeedbackService.getExistingFeedback(record.getId());
            vo.setAiFeedback(feedback);
        } catch (BusinessException ignored) {
            // 编程题没有现成反馈时，不影响本地判题结果。
        }
    }

    private SubmitResultVO toSubmitResultVO(SubmitRecord record, List<TestCaseJudgeResult> caseResults) {
        SubmitResultVO vo = new SubmitResultVO();
        vo.setSubmitId(record.getId());
        vo.setProblemId(record.getProblemId());
        vo.setJudgeStatus(record.getJudgeStatus());
        vo.setPassCount(record.getPassCount());
        vo.setTotalCount(record.getTotalCount());
        vo.setRunTime(record.getRunTime());
        vo.setNeedAiFeedback(record.getNeedAiFeedback());
        vo.setCodeHash(record.getCodeHash());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setOutputResult(record.getOutputResult());
        vo.setScore(record.getScore());
        vo.setErrorFingerprint(record.getErrorFingerprint());
        vo.setTestCaseResults(caseResults.stream().map(this::toTestCaseResultVO).toList());
        return vo;
    }

    private TestCaseResultVO toTestCaseResultVO(TestCaseJudgeResult caseResult) {
        TestCaseResultVO vo = new TestCaseResultVO();
        BeanUtils.copyProperties(caseResult, vo);
        return vo;
    }
}
