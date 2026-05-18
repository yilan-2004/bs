package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.service.agent.CodeContext;
import com.agentedu.vo.KnowledgeChunkVO;

import java.util.List;

public interface KnowledgeRetrievalService {

    List<KnowledgeChunkVO> retrieve(Problem problem, CodeContext context, String errorType, int topK);

    void enrichContext(Problem problem, CodeContext context, String errorType);
}
