package com.agentedu.service.impl;

import com.agentedu.common.PageResult;
import com.agentedu.dto.KnowledgeDocumentAddDTO;
import com.agentedu.dto.KnowledgeDocumentQueryDTO;
import com.agentedu.dto.KnowledgeDocumentUpdateDTO;
import com.agentedu.entity.KnowledgeBase;
import com.agentedu.entity.KnowledgeChunk;
import com.agentedu.entity.KnowledgeDocument;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.KnowledgeChunkMapper;
import com.agentedu.mapper.KnowledgeDocumentMapper;
import com.agentedu.service.KnowledgeDocumentService;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.KnowledgeChunkVO;
import com.agentedu.vo.KnowledgeDocumentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeDocumentService {

    private static final int ENABLED_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private static final int CHUNK_MAX_LENGTH = 900;

    private final KnowledgeBaseServiceImpl knowledgeBaseService;

    private final KnowledgeChunkMapper chunkMapper;

    @Override
    public Long add(KnowledgeDocumentAddDTO dto) {
        RoleAuthUtils.requireTeacher();
        knowledgeBaseService.requireOwnBase(dto.getBaseId());
        KnowledgeDocument document = new KnowledgeDocument();
        BeanUtils.copyProperties(dto, document);
        document.setStatus(ENABLED_STATUS);
        document.setChunkCount(0);
        save(document);
        return document.getId();
    }

    @Override
    public void update(KnowledgeDocumentUpdateDTO dto) {
        RoleAuthUtils.requireTeacher();
        KnowledgeDocument old = requireOwnDocument(dto.getId());
        knowledgeBaseService.requireOwnBase(dto.getBaseId());
        KnowledgeDocument document = new KnowledgeDocument();
        BeanUtils.copyProperties(dto, document);
        document.setChunkCount(old.getChunkCount());
        updateById(document);
    }

    @Override
    public void disable(Long id) {
        RoleAuthUtils.requireTeacher();
        requireOwnDocument(id);
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(id);
        document.setStatus(DISABLED_STATUS);
        updateById(document);
    }

    @Override
    public PageResult<KnowledgeDocumentVO> list(KnowledgeDocumentQueryDTO queryDTO) {
        RoleAuthUtils.requireTeacher();
        normalizePage(queryDTO);
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getBaseId() != null) {
            knowledgeBaseService.requireOwnBase(queryDTO.getBaseId());
            wrapper.eq(KnowledgeDocument::getBaseId, queryDTO.getBaseId());
        } else {
            List<Long> ownBaseIds = knowledgeBaseService.list(new com.agentedu.dto.KnowledgeBaseQueryDTO() {{
                setPageNum(1L);
                setPageSize(100L);
            }}).getRecords().stream().map(KnowledgeBaseVO -> KnowledgeBaseVO.getId()).toList();
            if (ownBaseIds.isEmpty()) {
                return new PageResult<>(0L, 0L, List.of());
            }
            wrapper.in(KnowledgeDocument::getBaseId, ownBaseIds);
        }
        wrapper.and(StringUtils.hasText(queryDTO.getKeyword()), item -> item
                .like(KnowledgeDocument::getTitle, queryDTO.getKeyword())
                .or()
                .like(KnowledgeDocument::getContent, queryDTO.getKeyword()));
        wrapper.eq(queryDTO.getStatus() != null, KnowledgeDocument::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(KnowledgeDocument::getCreateTime);
        Page<KnowledgeDocument> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        return new PageResult<>(page.getTotal(), page.getPages(), page.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    public KnowledgeDocumentVO detail(Long id) {
        RoleAuthUtils.requireTeacher();
        return toVO(requireOwnDocument(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer chunk(Long id) {
        RoleAuthUtils.requireTeacher();
        KnowledgeDocument document = requireOwnDocument(id);
        KnowledgeBase base = knowledgeBaseService.requireOwnBase(document.getBaseId());
        chunkMapper.delete(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, document.getId()));
        List<String> pieces = splitContent(document.getContent());
        int order = 1;
        for (String piece : pieces) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setBaseId(base.getId());
            chunk.setDocumentId(document.getId());
            chunk.setSubjectId(base.getSubjectId());
            chunk.setDocumentTitle(document.getTitle());
            chunk.setKnowledgeTags(document.getKnowledgeTags());
            chunk.setChunkText(piece);
            chunk.setChunkOrder(order++);
            chunk.setStatus(ENABLED_STATUS);
            chunkMapper.insert(chunk);
        }
        KnowledgeDocument update = new KnowledgeDocument();
        update.setId(document.getId());
        update.setChunkCount(pieces.size());
        updateById(update);
        return pieces.size();
    }

    @Override
    public List<KnowledgeChunkVO> chunks(Long documentId) {
        RoleAuthUtils.requireTeacher();
        requireOwnDocument(documentId);
        return chunkMapper.selectList(new LambdaQueryWrapper<KnowledgeChunk>()
                        .eq(KnowledgeChunk::getDocumentId, documentId)
                        .eq(KnowledgeChunk::getStatus, ENABLED_STATUS)
                        .orderByAsc(KnowledgeChunk::getChunkOrder))
                .stream()
                .map(this::toChunkVO)
                .toList();
    }

    private KnowledgeDocument requireOwnDocument(Long id) {
        KnowledgeDocument document = getById(id);
        if (document == null) {
            throw new BusinessException("知识文档不存在");
        }
        knowledgeBaseService.requireOwnBase(document.getBaseId());
        return document;
    }

    private KnowledgeDocumentVO toVO(KnowledgeDocument document) {
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        BeanUtils.copyProperties(document, vo);
        KnowledgeBase base = knowledgeBaseService.requireOwnBase(document.getBaseId());
        vo.setBaseName(base.getName());
        return vo;
    }

    private KnowledgeChunkVO toChunkVO(KnowledgeChunk chunk) {
        KnowledgeChunkVO vo = new KnowledgeChunkVO();
        BeanUtils.copyProperties(chunk, vo);
        return vo;
    }

    private List<String> splitContent(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String block : content.replace("\r\n", "\n").split("\\n\\s*\\n|\\n")) {
            String normalized = block.trim();
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            if (normalized.length() <= CHUNK_MAX_LENGTH) {
                result.add(normalized);
                continue;
            }
            for (int start = 0; start < normalized.length(); start += CHUNK_MAX_LENGTH) {
                result.add(normalized.substring(start, Math.min(start + CHUNK_MAX_LENGTH, normalized.length())));
            }
        }
        return result;
    }

    private void normalizePage(KnowledgeDocumentQueryDTO queryDTO) {
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
}
