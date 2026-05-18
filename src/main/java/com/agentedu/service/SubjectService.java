package com.agentedu.service;

import com.agentedu.common.PageResult;
import com.agentedu.dto.SubjectAddDTO;
import com.agentedu.dto.SubjectQueryDTO;
import com.agentedu.dto.SubjectUpdateDTO;
import com.agentedu.entity.Subject;
import com.agentedu.vo.SubjectVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SubjectService extends IService<Subject> {

    Long addSubject(SubjectAddDTO dto);

    void updateSubject(SubjectUpdateDTO dto);

    void disableSubject(Long id);

    PageResult<SubjectVO> listSubjects(SubjectQueryDTO queryDTO);

    SubjectVO getSubjectDetail(Long id);

    Subject requireEnabledSubject(Long subjectId);

    Long getDefaultProgrammingSubjectId();
}
