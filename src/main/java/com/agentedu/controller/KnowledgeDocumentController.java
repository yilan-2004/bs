package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.KnowledgeDocumentAddDTO;
import com.agentedu.dto.KnowledgeDocumentQueryDTO;
import com.agentedu.dto.KnowledgeDocumentUpdateDTO;
import com.agentedu.service.KnowledgeDocumentService;
import com.agentedu.vo.KnowledgeChunkVO;
import com.agentedu.vo.KnowledgeDocumentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/knowledge-document")
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody KnowledgeDocumentAddDTO dto) {
        return Result.success(knowledgeDocumentService.add(dto));
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody KnowledgeDocumentUpdateDTO dto) {
        knowledgeDocumentService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeDocumentService.disable(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<KnowledgeDocumentVO>> list(KnowledgeDocumentQueryDTO queryDTO) {
        return Result.success(knowledgeDocumentService.list(queryDTO));
    }

    @GetMapping("/detail/{id}")
    public Result<KnowledgeDocumentVO> detail(@PathVariable Long id) {
        return Result.success(knowledgeDocumentService.detail(id));
    }

    @PostMapping("/chunk/{id}")
    public Result<Integer> chunk(@PathVariable Long id) {
        return Result.success(knowledgeDocumentService.chunk(id));
    }

    @GetMapping("/chunks/{documentId}")
    public Result<List<KnowledgeChunkVO>> chunks(@PathVariable Long documentId) {
        return Result.success(knowledgeDocumentService.chunks(documentId));
    }
}
