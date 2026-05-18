package com.agentedu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.common.PageResult;
import com.agentedu.dto.KnowledgeBaseAddDTO;
import com.agentedu.dto.KnowledgeBaseQueryDTO;
import com.agentedu.dto.KnowledgeBaseUpdateDTO;
import com.agentedu.entity.KnowledgeBase;
import com.agentedu.entity.KnowledgeChunk;
import com.agentedu.entity.KnowledgeDocument;
import com.agentedu.entity.Subject;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.KnowledgeBaseMapper;
import com.agentedu.mapper.KnowledgeChunkMapper;
import com.agentedu.mapper.KnowledgeDocumentMapper;
import com.agentedu.service.KnowledgeBaseService;
import com.agentedu.service.SubjectService;
import com.agentedu.utils.RoleAuthUtils;
import com.agentedu.vo.KnowledgeBaseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService {

    private static final int ENABLED_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private final SubjectService subjectService;

    private final KnowledgeDocumentMapper documentMapper;

    private final KnowledgeChunkMapper chunkMapper;

    @Override
    public Long add(KnowledgeBaseAddDTO dto) {
        RoleAuthUtils.requireTeacher();
        KnowledgeBase base = new KnowledgeBase();
        BeanUtils.copyProperties(dto, base);
        if (dto.getSubjectId() != null) {
            subjectService.requireEnabledSubject(dto.getSubjectId());
        }
        base.setCreatorId(StpUtil.getLoginIdAsLong());
        base.setStatus(ENABLED_STATUS);
        base.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        save(base);
        return base.getId();
    }

    @Override
    public void update(KnowledgeBaseUpdateDTO dto) {
        RoleAuthUtils.requireTeacher();
        KnowledgeBase old = requireOwnBase(dto.getId());
        if (dto.getSubjectId() != null) {
            subjectService.requireEnabledSubject(dto.getSubjectId());
        }
        KnowledgeBase base = new KnowledgeBase();
        BeanUtils.copyProperties(dto, base);
        base.setCreatorId(old.getCreatorId());
        base.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        updateById(base);
    }

    @Override
    public void disable(Long id) {
        RoleAuthUtils.requireTeacher();
        requireOwnBase(id);
        KnowledgeBase base = new KnowledgeBase();
        base.setId(id);
        base.setStatus(DISABLED_STATUS);
        updateById(base);
    }

    @Override
    public PageResult<KnowledgeBaseVO> list(KnowledgeBaseQueryDTO queryDTO) {
        RoleAuthUtils.requireTeacher();
        normalizePage(queryDTO);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getCreatorId, StpUtil.getLoginIdAsLong());
        wrapper.and(StringUtils.hasText(queryDTO.getKeyword()), item -> item
                .like(KnowledgeBase::getName, queryDTO.getKeyword())
                .or()
                .like(KnowledgeBase::getDescription, queryDTO.getKeyword()));
        wrapper.eq(queryDTO.getSubjectId() != null, KnowledgeBase::getSubjectId, queryDTO.getSubjectId());
        wrapper.eq(queryDTO.getStatus() != null, KnowledgeBase::getStatus, queryDTO.getStatus());
        wrapper.orderByAsc(KnowledgeBase::getSortOrder).orderByDesc(KnowledgeBase::getCreateTime);
        Page<KnowledgeBase> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<KnowledgeBaseVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page.getTotal(), page.getPages(), records);
    }

    @Override
    public KnowledgeBaseVO detail(Long id) {
        RoleAuthUtils.requireTeacher();
        return toVO(requireOwnBase(id));
    }

    KnowledgeBase requireOwnBase(Long id) {
        KnowledgeBase base = getById(id);
        if (base == null) {
            throw new BusinessException("知识库不存在");
        }
        if (!Long.valueOf(StpUtil.getLoginIdAsLong()).equals(base.getCreatorId())) {
            throw new BusinessException("无权管理该知识库");
        }
        return base;
    }

    private KnowledgeBaseVO toVO(KnowledgeBase base) {
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtils.copyProperties(base, vo);
        if (base.getSubjectId() != null) {
            Subject subject = subjectService.requireEnabledSubject(base.getSubjectId());
            vo.setSubjectName(subject.getName());
        }
        vo.setDocumentCount(Math.toIntExact(documentMapper.selectCount(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getBaseId, base.getId())
                .eq(KnowledgeDocument::getStatus, ENABLED_STATUS))));
        vo.setChunkCount(Math.toIntExact(chunkMapper.selectCount(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getBaseId, base.getId())
                .eq(KnowledgeChunk::getStatus, ENABLED_STATUS))));
        return vo;
    }

    private void normalizePage(KnowledgeBaseQueryDTO queryDTO) {
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
