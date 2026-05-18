package com.agentedu.service;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.dto.TestCaseAddDTO;
import com.agentedu.dto.TestCaseUpdateDTO;
import com.agentedu.entity.Problem;
import com.agentedu.entity.ProblemBank;
import com.agentedu.entity.TestCase;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.ProblemBankMapper;
import com.agentedu.mapper.ProblemMapper;
import com.agentedu.mapper.TestCaseMapper;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.TestCaseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseService extends ServiceImpl<TestCaseMapper, TestCase> {

    private static final int ENABLED_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private static final int SAMPLE_CASE = 1;

    private static final int HIDDEN_CASE = 0;

    private final ProblemMapper problemMapper;

    private final ProblemBankMapper problemBankMapper;

    /**
     * Teacher adds test cases only to problems created by himself or herself.
     */
    public Long addTestCase(TestCaseAddDTO dto) {
        RoleAuthUtils.requireTeacher();
        Problem problem = checkProblemExists(dto.getProblemId());
        checkTeacherOwnsProblem(problem);

        TestCase testCase = new TestCase();
        BeanUtils.copyProperties(dto, testCase);
        testCase.setIsSample(dto.getIsSample() == null ? HIDDEN_CASE : dto.getIsSample());
        testCase.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        testCase.setStatus(ENABLED_STATUS);
        save(testCase);
        return testCase.getId();
    }

    /**
     * Teacher updates only test cases that belong to his or her own problems.
     */
    public void updateTestCase(TestCaseUpdateDTO dto) {
        RoleAuthUtils.requireTeacher();

        TestCase oldCase = getById(dto.getId());
        if (oldCase == null) {
            throw new BusinessException("Test case does not exist");
        }
        checkTeacherOwnsProblem(checkProblemExists(oldCase.getProblemId()));

        Problem targetProblem = checkProblemExists(dto.getProblemId());
        checkTeacherOwnsProblem(targetProblem);

        TestCase testCase = new TestCase();
        BeanUtils.copyProperties(dto, testCase);
        testCase.setIsSample(dto.getIsSample() == null ? HIDDEN_CASE : dto.getIsSample());
        testCase.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        updateById(testCase);
    }

    /**
     * Teacher disables only test cases that belong to his or her own problems.
     */
    public void disableTestCase(Long id) {
        RoleAuthUtils.requireTeacher();
        TestCase oldCase = getById(id);
        if (oldCase == null) {
            throw new BusinessException("Test case does not exist");
        }
        checkTeacherOwnsProblem(checkProblemExists(oldCase.getProblemId()));

        TestCase testCase = new TestCase();
        testCase.setId(id);
        testCase.setStatus(DISABLED_STATUS);
        updateById(testCase);
    }

    /**
     * Teacher can view all enabled test cases only for his or her own problem.
     */
    public List<TestCaseVO> listAllEnabledByProblem(Long problemId) {
        RoleAuthUtils.requireTeacher();
        Problem problem = checkProblemExists(problemId);
        checkTeacherOwnsProblem(problem);
        return list(new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getProblemId, problemId)
                .eq(TestCase::getStatus, ENABLED_STATUS)
                .orderByAsc(TestCase::getSortOrder)
                .orderByAsc(TestCase::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * Students can only see enabled sample cases. Hidden cases are never returned here.
     */
    public List<TestCaseVO> listSampleByProblem(Long problemId) {
        Problem problem = checkProblemExists(problemId);
        if (RoleAuthUtils.isStudent() && !Integer.valueOf(ENABLED_STATUS).equals(problem.getStatus())) {
            throw new BusinessException("Problem does not exist or has been disabled");
        }
        if (RoleAuthUtils.isStudent()) {
            checkStudentCanReadBank(problem.getBankId());
        }

        return list(new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getProblemId, problemId)
                .eq(TestCase::getStatus, ENABLED_STATUS)
                .eq(TestCase::getIsSample, SAMPLE_CASE)
                .orderByAsc(TestCase::getSortOrder)
                .orderByAsc(TestCase::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private Problem checkProblemExists(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException("Problem does not exist");
        }
        return problem;
    }

    private void checkTeacherOwnsProblem(Problem problem) {
        if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(problem.getCreatorId())) {
            throw new BusinessException("No permission to manage test cases for this problem");
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

    private TestCaseVO toVO(TestCase testCase) {
        TestCaseVO vo = new TestCaseVO();
        BeanUtils.copyProperties(testCase, vo);
        return vo;
    }
}
