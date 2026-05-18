package com.agentedu.controller;

import com.agentedu.common.PageResult;
import com.agentedu.common.Result;
import com.agentedu.dto.ProblemBankAddDTO;
import com.agentedu.dto.ProblemBankQueryDTO;
import com.agentedu.dto.ProblemBankUpdateDTO;
import com.agentedu.dto.ProblemQueryDTO;
import com.agentedu.service.ProblemBankService;
import com.agentedu.vo.ProblemBankVO;
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
@RequestMapping("/problem-bank")
@RequiredArgsConstructor
public class ProblemBankController {

    private final ProblemBankService problemBankService;

    /**
     * Teacher creates a problem bank.
     */
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody ProblemBankAddDTO dto) {
        return Result.success(problemBankService.addBank(dto));
    }

    /**
     * Teacher updates a problem bank created by himself or herself.
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody ProblemBankUpdateDTO dto) {
        problemBankService.updateBank(dto);
        return Result.success();
    }

    /**
     * Teacher logically disables a problem bank.
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        problemBankService.disableBank(id);
        return Result.success();
    }

    /**
     * Page query for problem banks.
     */
    @GetMapping("/list")
    public Result<PageResult<ProblemBankVO>> list(ProblemBankQueryDTO queryDTO) {
        return Result.success(problemBankService.listBanks(queryDTO));
    }

    /**
     * Problem bank detail.
     */
    @GetMapping("/detail/{id}")
    public Result<ProblemBankVO> detail(@PathVariable Long id) {
        return Result.success(problemBankService.getBankDetail(id));
    }

    /**
     * Query problems under a bank.
     */
    @GetMapping("/problems/{bankId}")
    public Result<PageResult<ProblemVO>> problems(@PathVariable Long bankId, ProblemQueryDTO queryDTO) {
        return Result.success(problemBankService.listProblemsByBank(bankId, queryDTO));
    }
}
