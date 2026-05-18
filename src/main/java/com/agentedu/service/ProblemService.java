package com.agentedu.service;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.common.PageResult;
import com.agentedu.dto.ProblemAddDTO;
import com.agentedu.dto.ProblemQueryDTO;
import com.agentedu.dto.ProblemUpdateDTO;
import com.agentedu.dto.QuestionOptionDTO;
import com.agentedu.entity.Problem;
import com.agentedu.entity.ProblemBank;
import com.agentedu.entity.QuestionOption;
import com.agentedu.entity.Subject;
import com.agentedu.enums.ProblemDifficultyEnum;
import com.agentedu.enums.QuestionTypeEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.ProblemBankMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.QuestionOptionMapper;
import com.agentedu.mapper.SubjectMapper;
import com.agentedu.service.SubjectService;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.QuestionOptionVO;
import com.agentedu.vo.ProblemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService extends ServiceImpl<ProblemMapper, Problem> {

    private static final int ENABLED_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private final ProblemBankMapper problemBankMapper;

    private final QuestionOptionMapper questionOptionMapper;

    private final SubjectMapper subjectMapper;

    private final SubjectService subjectService;

    /**
     * Teacher creates a problem. Optional bankId must point to an enabled bank owned by the teacher.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addProblem(ProblemAddDTO dto) {
        RoleAuthUtils.requireTeacher();
        checkDifficulty(dto.getDifficulty());
        checkQuestionType(dto.getQuestionType());
        checkWritableBank(dto.getBankId());
        Long subjectId = normalizeSubjectId(dto.getSubjectId(), dto.getBankId());
        String questionType = defaultQuestionType(dto.getQuestionType());
        validateQuestionContent(questionType, dto.getStandardAnswer(), dto.getOptions());

        Problem problem = new Problem();
        BeanUtils.copyProperties(dto, problem);
        problem.setDifficulty(defaultDifficulty(dto.getDifficulty()));
        problem.setSubjectId(subjectId);
        problem.setQuestionType(questionType);
        problem.setStandardAnswer(normalizeStandardAnswerForSave(questionType, dto.getStandardAnswer()));
        problem.setScoringPoints(normalizeScoringPointsForSave(questionType, dto.getScoringPoints()));
        problem.setScore(normalizeProblemScore(questionType, dto.getScore()));
        problem.setCreatorId(StpUtil.getLoginIdAsLong());
        problem.setStatus(ENABLED_STATUS);
        save(problem);
        saveChoiceOptions(problem.getId(), questionType, dto.getOptions());
        return problem.getId();
    }

    /**
     * Teacher updates only the problems created by himself or herself.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProblem(ProblemUpdateDTO dto) {
        RoleAuthUtils.requireTeacher();
        checkDifficulty(dto.getDifficulty());
        checkQuestionType(dto.getQuestionType());

        Problem oldProblem = getById(dto.getId());
        if (oldProblem == null) {
            throw new BusinessException("Problem does not exist");
        }
        checkTeacherOwnsProblem(oldProblem);
        checkWritableBank(dto.getBankId());
        Long subjectId = normalizeSubjectId(dto.getSubjectId(), dto.getBankId());
        String questionType = defaultQuestionType(dto.getQuestionType());
        validateQuestionContent(questionType, dto.getStandardAnswer(), dto.getOptions());

        Problem problem = new Problem();
        BeanUtils.copyProperties(dto, problem);
        problem.setDifficulty(defaultDifficulty(dto.getDifficulty()));
        problem.setSubjectId(subjectId);
        problem.setQuestionType(questionType);
        problem.setStandardAnswer(normalizeStandardAnswerForSave(questionType, dto.getStandardAnswer()));
        problem.setScoringPoints(normalizeScoringPointsForSave(questionType, dto.getScoringPoints()));
        problem.setScore(normalizeProblemScore(questionType, dto.getScore()));
        updateById(problem);
        refreshChoiceOptions(dto.getId(), questionType, dto.getOptions());
    }

    /**
     * Teacher disables only the problems created by himself or herself.
     */
    public void disableProblem(Long id) {
        RoleAuthUtils.requireTeacher();
        Problem oldProblem = getById(id);
        if (oldProblem == null) {
            throw new BusinessException("Problem does not exist");
        }
        checkTeacherOwnsProblem(oldProblem);

        Problem problem = new Problem();
        problem.setId(id);
        problem.setStatus(DISABLED_STATUS);
        updateById(problem);
    }

    /**
     * Page query. Students only see enabled problems under enabled banks; teachers see their own problems.
     */
    public PageResult<ProblemVO> listProblems(ProblemQueryDTO queryDTO) {
        normalizePage(queryDTO);
        checkDifficulty(queryDTO.getDifficulty());
        checkQuestionType(queryDTO.getQuestionType());
        checkReadableBankForQuery(queryDTO.getBankId());

        LambdaQueryWrapper<Problem> wrapper = buildQueryWrapper(queryDTO);
        Page<Problem> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<ProblemVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page.getTotal(), page.getPages(), records);
    }

    /**
     * Problem detail. Disabled problems or problems under disabled banks are hidden from students.
     */
    public ProblemVO getProblemDetail(Long id) {
        Problem problem = getById(id);
        if (problem == null) {
            throw new BusinessException("Problem does not exist");
        }
        if (RoleAuthUtils.isStudent()) {
            if (!Integer.valueOf(ENABLED_STATUS).equals(problem.getStatus())) {
                throw new BusinessException("Problem does not exist or has been disabled");
            }
            checkStudentCanReadBank(problem.getBankId());
        } else if (RoleAuthUtils.isTeacher()) {
            checkTeacherOwnsProblem(problem);
        }
        return toVO(problem);
    }

    private LambdaQueryWrapper<Problem> buildQueryWrapper(ProblemQueryDTO queryDTO) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Problem::getTitle, queryDTO.getTitle());
        wrapper.eq(StringUtils.hasText(queryDTO.getDifficulty()), Problem::getDifficulty, queryDTO.getDifficulty());
        wrapper.like(StringUtils.hasText(queryDTO.getKnowledgeTags()), Problem::getKnowledgeTags, queryDTO.getKnowledgeTags());
        wrapper.eq(queryDTO.getBankId() != null, Problem::getBankId, queryDTO.getBankId());
        wrapper.eq(queryDTO.getSubjectId() != null, Problem::getSubjectId, queryDTO.getSubjectId());
        wrapper.eq(StringUtils.hasText(queryDTO.getQuestionType()), Problem::getQuestionType, queryDTO.getQuestionType());

        if (RoleAuthUtils.isTeacher()) {
            wrapper.eq(Problem::getCreatorId, StpUtil.getLoginIdAsLong());
            if (queryDTO.getStatus() != null) {
                wrapper.eq(Problem::getStatus, queryDTO.getStatus());
            } else {
                wrapper.eq(Problem::getStatus, ENABLED_STATUS);
            }
        } else {
            wrapper.eq(Problem::getStatus, ENABLED_STATUS);
            List<Long> enabledBankIds = getEnabledBankIds();
            if (enabledBankIds.isEmpty()) {
                wrapper.isNull(Problem::getBankId);
            } else {
                wrapper.and(item -> item.isNull(Problem::getBankId).or().in(Problem::getBankId, enabledBankIds));
            }
            List<Long> enabledSubjectIds = getEnabledSubjectIds();
            if (!enabledSubjectIds.isEmpty()) {
                wrapper.and(item -> item.isNull(Problem::getSubjectId).or().in(Problem::getSubjectId, enabledSubjectIds));
            } else {
                wrapper.isNull(Problem::getSubjectId);
            }
        }
        wrapper.orderByDesc(Problem::getCreateTime);
        return wrapper;
    }

    private void checkWritableBank(Long bankId) {
        if (bankId == null) {
            return;
        }
        ProblemBank bank = problemBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException("Problem bank does not exist");
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(bank.getStatus())) {
            throw new BusinessException("Problem bank has been disabled");
        }
        if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(bank.getCreatorId())) {
            throw new BusinessException("No permission to add problem to this bank");
        }
    }

    private void checkReadableBankForQuery(Long bankId) {
        if (bankId == null) {
            return;
        }
        ProblemBank bank = problemBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException("Problem bank does not exist");
        }
        if (RoleAuthUtils.isStudent() && !Integer.valueOf(ENABLED_STATUS).equals(bank.getStatus())) {
            throw new BusinessException("Problem bank does not exist or has been disabled");
        }
        if (RoleAuthUtils.isTeacher() && !Long.valueOf(StpUtil.getLoginIdAsLong()).equals(bank.getCreatorId())) {
            throw new BusinessException("No permission to view this problem bank");
        }
    }

    private void checkStudentCanReadBank(Long bankId) {
        if (bankId == null) {
            return;
        }
        ProblemBank bank = problemBankMapper.selectById(bankId);
        if (bank == null || !Integer.valueOf(ENABLED_STATUS).equals(bank.getStatus())) {
            throw new BusinessException("Problem does not exist or has been disabled");
        }
    }

    private List<Long> getEnabledBankIds() {
        return problemBankMapper.selectList(new LambdaQueryWrapper<ProblemBank>()
                        .eq(ProblemBank::getStatus, ENABLED_STATUS))
                .stream()
                .map(ProblemBank::getId)
                .toList();
    }

    private List<Long> getEnabledSubjectIds() {
        return subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getStatus, ENABLED_STATUS))
                .stream()
                .map(Subject::getId)
                .toList();
    }

    private void normalizePage(ProblemQueryDTO queryDTO) {
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

    private void checkDifficulty(String difficulty) {
        if (StringUtils.hasText(difficulty) && !ProblemDifficultyEnum.isValid(difficulty)) {
            throw new BusinessException("Problem difficulty must be EASY, MEDIUM, or HARD");
        }
    }

    private void checkQuestionType(String questionType) {
        if (StringUtils.hasText(questionType) && !QuestionTypeEnum.isValid(questionType)) {
            throw new BusinessException("Question type must be PROGRAMMING, CHOICE, FILL_BLANK, or SHORT_ANSWER");
        }
    }

    private String defaultDifficulty(String difficulty) {
        return StringUtils.hasText(difficulty) ? difficulty : ProblemDifficultyEnum.EASY.name();
    }

    private String defaultQuestionType(String questionType) {
        return StringUtils.hasText(questionType) ? questionType : QuestionTypeEnum.PROGRAMMING.name();
    }

    private void checkTeacherOwnsProblem(Problem problem) {
        if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(problem.getCreatorId())) {
            throw new BusinessException("No permission to manage this problem");
        }
    }

    private ProblemVO toVO(Problem problem) {
        ProblemVO vo = new ProblemVO();
        BeanUtils.copyProperties(problem, vo);
        if (problem.getBankId() != null) {
            ProblemBank bank = problemBankMapper.selectById(problem.getBankId());
            if (bank != null) {
                vo.setBankName(bank.getName());
            }
        }
        if (problem.getSubjectId() != null) {
            Subject subject = subjectMapper.selectById(problem.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }
        }
        vo.setOptions(listOptionVOs(problem.getId()));
        if (RoleAuthUtils.isStudent()) {
            vo.setStandardAnswer(null);
            vo.setScoringPoints(null);
            vo.getOptions().forEach(option -> option.setIsCorrect(null));
        }
        return vo;
    }

    private void validateQuestionContent(String questionType, String standardAnswer, List<QuestionOptionDTO> options) {
        if (QuestionTypeEnum.CHOICE.name().equals(questionType)) {
            if (options == null || options.size() < 2) {
                throw new BusinessException("选择题至少需要2个选项");
            }
            long correctCount = options.stream()
                    .filter(item -> Integer.valueOf(1).equals(item.getIsCorrect()))
                    .count();
            if (correctCount != 1) {
                throw new BusinessException("第一版选择题仅支持单选，必须且只能设置1个正确选项");
            }
            for (QuestionOptionDTO option : options) {
                if (!StringUtils.hasText(option.getOptionKey()) || !StringUtils.hasText(option.getOptionContent())) {
                    throw new BusinessException("选项标识和选项内容不能为空");
                }
            }
        }
        if (QuestionTypeEnum.FILL_BLANK.name().equals(questionType)
                && !StringUtils.hasText(standardAnswer)) {
            throw new BusinessException("填空题标准答案不能为空");
        }
        if (QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType)
                && !StringUtils.hasText(standardAnswer)) {
            throw new BusinessException("Short answer reference answer cannot be empty");
        }
    }

    private String normalizeStandardAnswerForSave(String questionType, String standardAnswer) {
        return QuestionTypeEnum.FILL_BLANK.name().equals(questionType)
                || QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType) ? standardAnswer : null;
    }

    private String normalizeScoringPointsForSave(String questionType, String scoringPoints) {
        return QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType) ? scoringPoints : null;
    }

    private Integer normalizeProblemScore(String questionType, Integer score) {
        if (!QuestionTypeEnum.SHORT_ANSWER.name().equals(questionType)) {
            return null;
        }
        if (score == null) {
            return 100;
        }
        if (score < 1 || score > 1000) {
            throw new BusinessException("Short answer score must be between 1 and 1000");
        }
        return score;
    }

    private void saveChoiceOptions(Long problemId, String questionType, List<QuestionOptionDTO> options) {
        if (!QuestionTypeEnum.CHOICE.name().equals(questionType)) {
            return;
        }
        for (int i = 0; i < options.size(); i++) {
            QuestionOptionDTO optionDTO = options.get(i);
            QuestionOption option = new QuestionOption();
            option.setProblemId(problemId);
            option.setOptionKey(optionDTO.getOptionKey().trim());
            option.setOptionContent(optionDTO.getOptionContent().trim());
            option.setIsCorrect(Integer.valueOf(1).equals(optionDTO.getIsCorrect()) ? 1 : 0);
            option.setSortOrder(optionDTO.getSortOrder() == null ? i : optionDTO.getSortOrder());
            questionOptionMapper.insert(option);
        }
    }

    private void refreshChoiceOptions(Long problemId, String questionType, List<QuestionOptionDTO> options) {
        questionOptionMapper.delete(new LambdaQueryWrapper<QuestionOption>()
                .eq(QuestionOption::getProblemId, problemId));
        saveChoiceOptions(problemId, questionType, options);
    }

    private List<QuestionOptionVO> listOptionVOs(Long problemId) {
        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getProblemId, problemId)
                        .orderByAsc(QuestionOption::getSortOrder)
                        .orderByAsc(QuestionOption::getId))
                .stream()
                .sorted(Comparator.comparing(QuestionOption::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(option -> {
                    QuestionOptionVO vo = new QuestionOptionVO();
                    BeanUtils.copyProperties(option, vo);
                    return vo;
                })
                .toList();
    }

    private Long normalizeSubjectId(Long subjectId, Long bankId) {
        Long resolvedSubjectId = subjectId;
        if (resolvedSubjectId == null && bankId != null) {
            ProblemBank bank = problemBankMapper.selectById(bankId);
            if (bank != null) {
                resolvedSubjectId = bank.getSubjectId();
            }
        }
        if (resolvedSubjectId == null) {
            resolvedSubjectId = subjectService.getDefaultProgrammingSubjectId();
        }
        if (resolvedSubjectId != null) {
            subjectService.requireEnabledSubject(resolvedSubjectId);
        }
        return resolvedSubjectId;
    }
}
