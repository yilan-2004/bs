package com.agentedu.service;

import com.agentedu.common.PageResult;
import com.agentedu.dto.SubmitCodeDTO;
import com.agentedu.dto.SubmitQueryDTO;
import com.agentedu.dto.SubmissionSubmitDTO;
import com.agentedu.vo.SubmitDetailVO;
import com.agentedu.vo.SubmitRecordVO;
import com.agentedu.vo.SubmitResultVO;

public interface SubmitService {

    /**
     * 学生提交代码，本阶段只保存记录，不执行真实评测。
     */
    SubmitResultVO submitCode(SubmitCodeDTO dto);

    /**
     * 多题型统一提交入口。
     */
    SubmitResultVO submitAnswer(SubmissionSubmitDTO dto);

    /**
     * 学生分页查看自己的提交记录。
     */
    PageResult<SubmitRecordVO> listMySubmissions(Long pageNum, Long pageSize);

    /**
     * 查看提交详情，学生只能看自己的提交，教师只能看自己创建题目的提交。
     */
    SubmitDetailVO getSubmitDetail(Long id);

    /**
     * 教师分页查看某道自己创建题目的提交记录。
     */
    PageResult<SubmitRecordVO> listByProblem(Long problemId, Long pageNum, Long pageSize);

    /**
     * 教师按条件分页查询自己创建题目的提交记录。
     */
    PageResult<SubmitRecordVO> listSubmissions(SubmitQueryDTO queryDTO);
}
