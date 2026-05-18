package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.KnowledgeBaseAddDTO;
import com.agentedu.dto.KnowledgeBaseQueryDTO;
import com.agentedu.dto.KnowledgeBaseUpdateDTO;
import com.agentedu.service.KnowledgeBaseService;
import com.agentedu.vo.KnowledgeBaseVO;
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

@RestController
@RequestMapping("/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody KnowledgeBaseAddDTO dto) {
        return Result.success(knowledgeBaseService.add(dto));
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody KnowledgeBaseUpdateDTO dto) {
        knowledgeBaseService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.disable(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<KnowledgeBaseVO>> list(KnowledgeBaseQueryDTO queryDTO) {
        return Result.success(knowledgeBaseService.list(queryDTO));
    }

    @GetMapping("/detail/{id}")
    public Result<KnowledgeBaseVO> detail(@PathVariable Long id) {
        return Result.success(knowledgeBaseService.detail(id));
    }
}
