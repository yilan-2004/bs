package com.agentedu.service;

import com.agentedu.common.PageResult;
import com.agentedu.dto.KnowledgeBaseAddDTO;
import com.agentedu.dto.KnowledgeBaseQueryDTO;
import com.agentedu.dto.KnowledgeBaseUpdateDTO;
import com.agentedu.vo.KnowledgeBaseVO;

public interface KnowledgeBaseService {

    Long add(KnowledgeBaseAddDTO dto);

    void update(KnowledgeBaseUpdateDTO dto);

    void disable(Long id);

    PageResult<KnowledgeBaseVO> list(KnowledgeBaseQueryDTO queryDTO);

    KnowledgeBaseVO detail(Long id);
}
