package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.dto.TestCaseAddDTO;
import com.agentedu.dto.TestCaseUpdateDTO;
import com.agentedu.service.TestCaseService;
import com.agentedu.vo.TestCaseVO;
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
@RequestMapping("/testcase")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    /**
     * 教师新增测试用例。
     */
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody TestCaseAddDTO dto) {
        return Result.success(testCaseService.addTestCase(dto));
    }

    /**
     * 教师修改测试用例。
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody TestCaseUpdateDTO dto) {
        testCaseService.updateTestCase(dto);
        return Result.success();
    }

    /**
     * 教师逻辑删除测试用例。
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        testCaseService.disableTestCase(id);
        return Result.success();
    }

    /**
     * 教师查看某道题的全部启用测试用例。
     */
    @GetMapping("/list/{problemId}")
    public Result<List<TestCaseVO>> list(@PathVariable Long problemId) {
        return Result.success(testCaseService.listAllEnabledByProblem(problemId));
    }

    /**
     * 学生查看某道题的启用样例测试用例。
     */
    @GetMapping("/sample/{problemId}")
    public Result<List<TestCaseVO>> sample(@PathVariable Long problemId) {
        return Result.success(testCaseService.listSampleByProblem(problemId));
    }
}
