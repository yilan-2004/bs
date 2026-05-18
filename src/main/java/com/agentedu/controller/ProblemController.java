package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.ProblemAddDTO;
import com.agentedu.dto.ProblemQueryDTO;
import com.agentedu.dto.ProblemUpdateDTO;
import com.agentedu.service.ProblemService;
import com.agentedu.vo.ProblemVO;
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
@RequestMapping("/problem")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    /**
     * 教师新增编程题。
     */
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody ProblemAddDTO dto) {
        return Result.success(problemService.addProblem(dto));
    }

    /**
     * 教师修改编程题。
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody ProblemUpdateDTO dto) {
        problemService.updateProblem(dto);
        return Result.success();
    }

    /**
     * 教师逻辑删除题目，将题目状态设置为禁用。
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        problemService.disableProblem(id);
        return Result.success();
    }

    /**
     * 登录用户分页查询题目列表。
     */
    @GetMapping("/list")
    public Result<PageResult<ProblemVO>> list(ProblemQueryDTO queryDTO) {
        return Result.success(problemService.listProblems(queryDTO));
    }

    /**
     * 登录用户查看题目详情。
     */
    @GetMapping("/detail/{id}")
    public Result<ProblemVO> detail(@PathVariable Long id) {
        return Result.success(problemService.getProblemDetail(id));
    }
}
