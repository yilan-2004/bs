package com.agentedu.service;

import com.agentedu.common.PageResult;
import com.agentedu.dto.ProblemBankAddDTO;
import com.agentedu.dto.ProblemBankQueryDTO;
import com.agentedu.dto.ProblemBankUpdateDTO;
import com.agentedu.dto.ProblemQueryDTO;
import com.agentedu.vo.ProblemBankVO;
import com.agentedu.vo.ProblemVO;

public interface ProblemBankService {

    Long addBank(ProblemBankAddDTO dto);

    void updateBank(ProblemBankUpdateDTO dto);

    void disableBank(Long id);

    PageResult<ProblemBankVO> listBanks(ProblemBankQueryDTO queryDTO);

    ProblemBankVO getBankDetail(Long id);

    PageResult<ProblemVO> listProblemsByBank(Long bankId, ProblemQueryDTO queryDTO);
}
