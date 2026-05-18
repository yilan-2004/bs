package com.agentedu.service;

import com.agentedu.common.PageResult;
import com.agentedu.dto.KnowledgeDocumentAddDTO;
import com.agentedu.dto.KnowledgeDocumentQueryDTO;
import com.agentedu.dto.KnowledgeDocumentUpdateDTO;
import com.agentedu.vo.KnowledgeChunkVO;
import com.agentedu.vo.KnowledgeDocumentVO;

import java.util.List;

public interface KnowledgeDocumentService {

    Long add(KnowledgeDocumentAddDTO dto);

    void update(KnowledgeDocumentUpdateDTO dto);

    void disable(Long id);

    PageResult<KnowledgeDocumentVO> list(KnowledgeDocumentQueryDTO queryDTO);

    KnowledgeDocumentVO detail(Long id);

    Integer chunk(Long id);

    List<KnowledgeChunkVO> chunks(Long documentId);
}
