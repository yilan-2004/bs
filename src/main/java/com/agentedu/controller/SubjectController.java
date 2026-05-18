package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.SubjectAddDTO;
import com.agentedu.dto.SubjectQueryDTO;
import com.agentedu.dto.SubjectUpdateDTO;
import com.agentedu.service.SubjectService;
import com.agentedu.vo.SubjectVO;
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
@RequestMapping("/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody SubjectAddDTO dto) {
        return Result.success(subjectService.addSubject(dto));
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody SubjectUpdateDTO dto) {
        subjectService.updateSubject(dto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.disableSubject(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<SubjectVO>> list(SubjectQueryDTO queryDTO) {
        return Result.success(subjectService.listSubjects(queryDTO));
    }

    @GetMapping("/detail/{id}")
    public Result<SubjectVO> detail(@PathVariable Long id) {
        return Result.success(subjectService.getSubjectDetail(id));
    }
}
