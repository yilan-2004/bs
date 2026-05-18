package com.agentedu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.common.PageResult;
import com.agentedu.dto.ProblemBankAddDTO;
import com.agentedu.dto.ProblemBankQueryDTO;
import com.agentedu.dto.ProblemBankUpdateDTO;
import com.agentedu.dto.ProblemQueryDTO;
import com.agentedu.entity.Problem;
import com.agentedu.entity.ProblemBank;
import com.agentedu.entity.Subject;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.ProblemBankMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.service.ProblemBankService;
import com.agentedu.service.ProblemService;
import com.agentedu.service.SubjectService;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.ProblemBankVO;
import com.agentedu.vo.ProblemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProblemBankServiceImpl extends ServiceImpl<ProblemBankMapper, ProblemBank> implements ProblemBankService {

    private static final int ENABLED_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private static final Set<String> BANK_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD", "MIXED");

    private final ProblemMapper problemMapper;

    private final ProblemService problemService;

    private final SubjectService subjectService;

    /**
     * Teacher creates a problem bank. The creator id is taken from the login session.
     */
    @Override
    public Long addBank(ProblemBankAddDTO dto) {
        RoleAuthUtils.requireTeacher();
        checkDifficulty(dto.getDifficulty());
        Long subjectId = normalizeSubjectId(dto.getSubjectId());

        ProblemBank bank = new ProblemBank();
        BeanUtils.copyProperties(dto, bank);
        bank.setDifficulty(defaultDifficulty(dto.getDifficulty()));
        bank.setSubjectId(subjectId);
        bank.setCreatorId(StpUtil.getLoginIdAsLong());
        bank.setStatus(ENABLED_STATUS);
        bank.setProblemCount(0);
        bank.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        save(bank);
        return bank.getId();
    }

    /**
     * Teacher updates only his or her own problem bank.
     */
    @Override
    public void updateBank(ProblemBankUpdateDTO dto) {
        RoleAuthUtils.requireTeacher();
        checkDifficulty(dto.getDifficulty());
        Long subjectId = normalizeSubjectId(dto.getSubjectId());

        ProblemBank oldBank = getById(dto.getId());
        if (oldBank == null) {
            throw new BusinessException("Problem bank does not exist");
        }
        checkTeacherOwnsBank(oldBank);

        ProblemBank bank = new ProblemBank();
        BeanUtils.copyProperties(dto, bank);
        bank.setDifficulty(defaultDifficulty(dto.getDifficulty()));
        bank.setSubjectId(subjectId);
        bank.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        updateById(bank);
    }

    /**
     * Teacher logically disables only his or her own problem bank.
     */
    @Override
    public void disableBank(Long id) {
        RoleAuthUtils.requireTeacher();
        ProblemBank bank = getById(id);
        if (bank == null) {
            throw new BusinessException("Problem bank does not exist");
        }
        checkTeacherOwnsBank(bank);

        ProblemBank update = new ProblemBank();
        update.setId(id);
        update.setStatus(DISABLED_STATUS);
        updateById(update);
    }

    /**
     * Students see enabled banks; teachers see only banks created by themselves.
     */
    @Override
    public PageResult<ProblemBankVO> listBanks(ProblemBankQueryDTO queryDTO) {
        normalizePage(queryDTO);
        checkDifficulty(queryDTO.getDifficulty());

        LambdaQueryWrapper<ProblemBank> wrapper = buildBankQueryWrapper(queryDTO);
        Page<ProblemBank> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<ProblemBankVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page.getTotal(), page.getPages(), records);
    }

    /**
     * Detail is protected by role and status.
     */
    @Override
    public ProblemBankVO getBankDetail(Long id) {
        ProblemBank bank = getById(id);
        if (bank == null) {
            throw new BusinessException("Problem bank does not exist");
        }
        if (RoleAuthUtils.isStudent() && !Integer.valueOf(ENABLED_STATUS).equals(bank.getStatus())) {
            throw new BusinessException("Problem bank does not exist or has been disabled");
        }
        if (RoleAuthUtils.isTeacher()) {
            checkTeacherOwnsBank(bank);
        }
        return toVO(bank);
    }

    /**
     * Query enabled problems under a bank. Student can only access enabled banks.
     */
    @Override
    public PageResult<ProblemVO> listProblemsByBank(Long bankId, ProblemQueryDTO queryDTO) {
        ProblemBank bank = getById(bankId);
        if (bank == null) {
            throw new BusinessException("Problem bank does not exist");
        }
        if (RoleAuthUtils.isStudent() && !Integer.valueOf(ENABLED_STATUS).equals(bank.getStatus())) {
            throw new BusinessException("Problem bank does not exist or has been disabled");
        }
        if (RoleAuthUtils.isTeacher()) {
            checkTeacherOwnsBank(bank);
        }
        queryDTO.setBankId(bankId);
        return problemService.listProblems(queryDTO);
    }

    private LambdaQueryWrapper<ProblemBank> buildBankQueryWrapper(ProblemBankQueryDTO queryDTO) {
        LambdaQueryWrapper<ProblemBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(queryDTO.getKeyword()), item -> item
                .like(ProblemBank::getName, queryDTO.getKeyword())
                .or()
                .like(ProblemBank::getDescription, queryDTO.getKeyword()));
        wrapper.eq(StringUtils.hasText(queryDTO.getDifficulty()), ProblemBank::getDifficulty, queryDTO.getDifficulty());
        wrapper.like(StringUtils.hasText(queryDTO.getKnowledgeTag()), ProblemBank::getKnowledgeTags, queryDTO.getKnowledgeTag());
        wrapper.eq(queryDTO.getSubjectId() != null, ProblemBank::getSubjectId, queryDTO.getSubjectId());

        if (RoleAuthUtils.isTeacher()) {
            wrapper.eq(ProblemBank::getCreatorId, StpUtil.getLoginIdAsLong());
            if (queryDTO.getStatus() != null) {
                wrapper.eq(ProblemBank::getStatus, queryDTO.getStatus());
            }
        } else {
            wrapper.eq(ProblemBank::getStatus, ENABLED_STATUS);
            List<Long> enabledSubjectIds = getEnabledSubjectIds();
            if (!enabledSubjectIds.isEmpty()) {
                wrapper.and(item -> item.isNull(ProblemBank::getSubjectId)
                        .or()
                        .in(ProblemBank::getSubjectId, enabledSubjectIds));
            } else {
                wrapper.isNull(ProblemBank::getSubjectId);
            }
        }
        wrapper.orderByAsc(ProblemBank::getSortOrder).orderByDesc(ProblemBank::getCreateTime);
        return wrapper;
    }

    private void normalizePage(ProblemBankQueryDTO queryDTO) {
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
        if (StringUtils.hasText(difficulty) && !BANK_DIFFICULTIES.contains(difficulty)) {
            throw new BusinessException("Problem bank difficulty must be EASY, MEDIUM, HARD, or MIXED");
        }
    }

    private String defaultDifficulty(String difficulty) {
        return StringUtils.hasText(difficulty) ? difficulty : "MIXED";
    }

    private void checkTeacherOwnsBank(ProblemBank bank) {
        if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(bank.getCreatorId())) {
            throw new BusinessException("No permission to manage this problem bank");
        }
    }

    private ProblemBankVO toVO(ProblemBank bank) {
        ProblemBankVO vo = new ProblemBankVO();
        BeanUtils.copyProperties(bank, vo);
        if (bank.getSubjectId() != null) {
            Subject subject = subjectService.getById(bank.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }
        }
        vo.setProblemCount(countEnabledProblems(bank.getId()));
        return vo;
    }

    private Long normalizeSubjectId(Long subjectId) {
        Long resolvedSubjectId = subjectId == null ? subjectService.getDefaultProgrammingSubjectId() : subjectId;
        if (resolvedSubjectId != null) {
            subjectService.requireEnabledSubject(resolvedSubjectId);
        }
        return resolvedSubjectId;
    }

    private Integer countEnabledProblems(Long bankId) {
        return Math.toIntExact(problemMapper.selectCount(new LambdaQueryWrapper<Problem>()
                .eq(Problem::getBankId, bankId)
                .eq(Problem::getStatus, ENABLED_STATUS)));
    }

    private List<Long> getEnabledSubjectIds() {
        return subjectService.list(new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getStatus, ENABLED_STATUS))
                .stream()
                .map(Subject::getId)
                .toList();
    }
}
