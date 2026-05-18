package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.SubmitCodeDTO;
import com.agentedu.dto.SubmitQueryDTO;
import com.agentedu.service.SubmitService;
import com.agentedu.vo.SubmitDetailVO;
import com.agentedu.vo.SubmitRecordVO;
import com.agentedu.vo.SubmitResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
public class SubmitController {

    private final SubmitService submitService;

    /**
     * 学生提交代码，本阶段只保存提交记录。
     */
    @PostMapping("/code")
    public Result<SubmitResultVO> submitCode(@Valid @RequestBody SubmitCodeDTO dto) {
        return Result.success(submitService.submitCode(dto));
    }

    /**
     * 学生分页查看自己的提交记录。
     */
    @GetMapping("/my")
    public Result<PageResult<SubmitRecordVO>> mySubmissions(@RequestParam(defaultValue = "1") Long pageNum,
                                                           @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(submitService.listMySubmissions(pageNum, pageSize));
    }

    /**
     * 查看提交详情，按当前角色做权限控制。
     */
    @GetMapping("/detail/{id}")
    public Result<SubmitDetailVO> detail(@PathVariable Long id) {
        return Result.success(submitService.getSubmitDetail(id));
    }

    /**
     * 教师查看某道自己创建题目的提交记录。
     */
    @GetMapping("/problem/{problemId}")
    public Result<PageResult<SubmitRecordVO>> problemSubmissions(@PathVariable Long problemId,
                                                                 @RequestParam(defaultValue = "1") Long pageNum,
                                                                 @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(submitService.listByProblem(problemId, pageNum, pageSize));
    }

    /**
     * 教师按条件查询自己创建题目的提交记录。
     */
    @GetMapping("/list")
    public Result<PageResult<SubmitRecordVO>> list(SubmitQueryDTO queryDTO) {
        return Result.success(submitService.listSubmissions(queryDTO));
    }
}
