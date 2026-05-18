package com.agentedu.service.impl;

import com.agentedu.entity.KnowledgeChunk;
import com.agentedu.entity.Problem;
import com.agentedu.mapper.KnowledgeChunkMapper;
import com.agentedu.service.KnowledgeRetrievalService;
import com.agentedu.service.agent.CodeContext;
import com.agentedu.vo.KnowledgeChunkVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalServiceImpl implements KnowledgeRetrievalService {

    private static final int ENABLED_STATUS = 1;

    private static final int DEFAULT_TOP_K = 3;

    private final KnowledgeChunkMapper chunkMapper;

    @Override
    public List<KnowledgeChunkVO> retrieve(Problem problem, CodeContext context, String errorType, int topK) {
        int limit = topK <= 0 ? DEFAULT_TOP_K : topK;
        List<String> keywords = buildKeywords(problem, context, errorType);
        LambdaQueryWrapper<KnowledgeChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeChunk::getStatus, ENABLED_STATUS);
        if (problem != null && problem.getSubjectId() != null) {
            wrapper.and(item -> item.eq(KnowledgeChunk::getSubjectId, problem.getSubjectId())
                    .or()
                    .isNull(KnowledgeChunk::getSubjectId));
        }
        if (!keywords.isEmpty()) {
            wrapper.and(item -> {
                for (int i = 0; i < keywords.size(); i++) {
                    if (i == 0) {
                        item.like(KnowledgeChunk::getChunkText, keywords.get(i));
                    } else {
                        item.or().like(KnowledgeChunk::getChunkText, keywords.get(i));
                    }
                }
            });
        }
        List<KnowledgeChunk> chunks = chunkMapper.selectList(wrapper);
        return chunks.stream()
                .map(chunk -> toScoredVO(chunk, problem, keywords))
                .filter(item -> item.getScore() > 0)
                .sorted(Comparator.comparing(KnowledgeChunkVO::getScore).reversed()
                        .thenComparing(KnowledgeChunkVO::getId))
                .limit(limit)
                .toList();
    }

    @Override
    public void enrichContext(Problem problem, CodeContext context, String errorType) {
        List<KnowledgeChunkVO> chunks = retrieve(problem, context, errorType, DEFAULT_TOP_K);
        if (chunks.isEmpty()) {
            context.setRagUsed(false);
            return;
        }
        context.setRagUsed(true);
        context.setEvidenceChunkIds(chunks.stream()
                .map(item -> String.valueOf(item.getId()))
                .collect(Collectors.joining(",")));
        context.setEvidenceSummary(chunks.stream()
                .map(item -> item.getDocumentTitle() + "#" + item.getChunkOrder() + ": " + abbreviate(item.getChunkText(), 180))
                .collect(Collectors.joining("\n")));
        context.setEvidenceText(chunks.stream()
                .map(item -> "Chunk " + item.getId() + " (" + item.getDocumentTitle() + "):\n"
                        + abbreviate(item.getChunkText(), 700))
                .collect(Collectors.joining("\n\n")));
    }

    private KnowledgeChunkVO toScoredVO(KnowledgeChunk chunk, Problem problem, List<String> keywords) {
        KnowledgeChunkVO vo = new KnowledgeChunkVO();
        BeanUtils.copyProperties(chunk, vo);
        int score = 0;
        if (problem != null && problem.getSubjectId() != null && problem.getSubjectId().equals(chunk.getSubjectId())) {
            score += 50;
        }
        Set<String> problemTags = splitTags(problem == null ? null : problem.getKnowledgeTags());
        Set<String> chunkTags = splitTags(chunk.getKnowledgeTags());
        for (String tag : problemTags) {
            if (chunkTags.contains(tag)) {
                score += 30;
            } else if (StringUtils.hasText(chunk.getChunkText()) && chunk.getChunkText().contains(tag)) {
                score += 16;
            }
        }
        String text = nullToEmpty(chunk.getChunkText()).toLowerCase();
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                score += 10;
            }
        }
        vo.setScore(score);
        return vo;
    }

    private List<String> buildKeywords(Problem problem, CodeContext context, String errorType) {
        Set<String> keywords = new LinkedHashSet<>();
        splitTags(problem == null ? null : problem.getKnowledgeTags()).forEach(keywords::add);
        addKeyword(keywords, errorType);
        addKeyword(keywords, problem == null ? null : problem.getTitle());
        addKeyword(keywords, context == null ? null : context.getProblemTitle());
        addKeyword(keywords, context == null ? null : context.getJudgeStatus());
        if (context != null) {
            addKeyword(keywords, context.getErrorMessage());
        }
        return new ArrayList<>(keywords).stream()
                .filter(item -> item.length() >= 2)
                .limit(10)
                .toList();
    }

    private void addKeyword(Set<String> keywords, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        String normalized = value.trim();
        if (normalized.length() > 24) {
            normalized = normalized.substring(0, 24);
        }
        keywords.add(normalized);
    }

    private Set<String> splitTags(String tags) {
        Set<String> result = new LinkedHashSet<>();
        if (!StringUtils.hasText(tags)) {
            return result;
        }
        for (String tag : tags.split("[,，;；\\s]+")) {
            String normalized = tag.trim();
            if (StringUtils.hasText(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return nullToEmpty(text);
        }
        return text.substring(0, maxLength) + "...";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
