package com.agentedu.service.impl;

import com.agentedu.common.PageResult;
import com.agentedu.dto.SubjectAddDTO;
import com.agentedu.dto.SubjectQueryDTO;
import com.agentedu.dto.SubjectUpdateDTO;
import com.agentedu.entity.Subject;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.SubjectMapper;
import com.agentedu.service.SubjectService;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.SubjectVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    private static final int ENABLED_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private static final String PROGRAMMING_SUBJECT_NAME = "编程";

    @Override
    public Long addSubject(SubjectAddDTO dto) {
        RoleAuthUtils.requireTeacher();
        Subject subject = new Subject();
        BeanUtils.copyProperties(dto, subject);
        subject.setStatus(ENABLED_STATUS);
        subject.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        save(subject);
        return subject.getId();
    }

    @Override
    public void updateSubject(SubjectUpdateDTO dto) {
        RoleAuthUtils.requireTeacher();
        Subject oldSubject = getById(dto.getId());
        if (oldSubject == null) {
            throw new BusinessException("Subject does not exist");
        }
        Subject subject = new Subject();
        BeanUtils.copyProperties(dto, subject);
        subject.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        updateById(subject);
    }

    @Override
    public void disableSubject(Long id) {
        RoleAuthUtils.requireTeacher();
        Subject oldSubject = getById(id);
        if (oldSubject == null) {
            throw new BusinessException("Subject does not exist");
        }
        Subject subject = new Subject();
        subject.setId(id);
        subject.setStatus(DISABLED_STATUS);
        updateById(subject);
    }

    @Override
    public PageResult<SubjectVO> listSubjects(SubjectQueryDTO queryDTO) {
        normalizePage(queryDTO);
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(queryDTO.getKeyword()), item -> item
                .like(Subject::getName, queryDTO.getKeyword())
                .or()
                .like(Subject::getDescription, queryDTO.getKeyword()));
        if (RoleAuthUtils.isStudent()) {
            wrapper.eq(Subject::getStatus, ENABLED_STATUS);
        } else if (queryDTO.getStatus() != null) {
            wrapper.eq(Subject::getStatus, queryDTO.getStatus());
        } else {
            wrapper.eq(Subject::getStatus, ENABLED_STATUS);
        }
        wrapper.orderByAsc(Subject::getSortOrder).orderByDesc(Subject::getCreateTime);
        Page<Subject> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<SubjectVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page.getTotal(), page.getPages(), records);
    }

    @Override
    public SubjectVO getSubjectDetail(Long id) {
        Subject subject = getById(id);
        if (subject == null) {
            throw new BusinessException("Subject does not exist");
        }
        if (RoleAuthUtils.isStudent() && !Integer.valueOf(ENABLED_STATUS).equals(subject.getStatus())) {
            throw new BusinessException("Subject does not exist or has been disabled");
        }
        return toVO(subject);
    }

    @Override
    public Subject requireEnabledSubject(Long subjectId) {
        Subject subject = getById(subjectId);
        if (subject == null) {
            throw new BusinessException("Subject does not exist");
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(subject.getStatus())) {
            throw new BusinessException("Subject has been disabled");
        }
        return subject;
    }

    @Override
    public Long getDefaultProgrammingSubjectId() {
        Subject subject = getOne(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getName, PROGRAMMING_SUBJECT_NAME)
                .eq(Subject::getStatus, ENABLED_STATUS)
                .last("LIMIT 1"));
        return subject == null ? null : subject.getId();
    }

    private void normalizePage(SubjectQueryDTO queryDTO) {
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1L);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20L);
        }
        if (queryDTO.getPageSize() > 100) {
            queryDTO.setPageSize(100L);
        }
    }

    private SubjectVO toVO(Subject subject) {
        SubjectVO vo = new SubjectVO();
        BeanUtils.copyProperties(subject, vo);
        return vo;
    }
}
