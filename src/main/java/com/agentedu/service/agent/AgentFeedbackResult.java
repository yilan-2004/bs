package com.agentedu.service.agent;

import lombok.Data;

@Data
public class AgentFeedbackResult {

    private String errorType;

    private String diagnosis;

    private String explanation;

    private String suggestion;

    private String evaluation;

    private String relatedKnowledge;

    private String nextPracticeAdvice;

    private Integer score;

    private String recommendProblems;

    private Boolean ragUsed;

    private String evidenceChunkIds;

    private String evidenceSummary;

    public static AgentFeedbackResult accepted() {
        AgentFeedbackResult result = new AgentFeedbackResult();
        result.setErrorType("通过");
        result.setDiagnosis("本次提交已经通过全部测试用例，说明代码在当前题目的输入输出要求下表现正确。");
        result.setExplanation("当前解法能够满足题目要求，可以回顾核心思路、输入处理和边界条件处理方式，形成可复用的解题经验。");
        result.setSuggestion("建议继续保持先理解题意、再用样例和边界数据验证代码的习惯。可以尝试优化变量命名和代码结构。");
        result.setEvaluation("表现良好，已经掌握本题核心知识点。继续用测试用例验证代码，会让解题过程更稳定。");
        result.setRelatedKnowledge("输入输出处理、基础表达式、测试用例验证");
        result.setNextPracticeAdvice("可以继续练习同知识点的变式题，重点关注边界输入和多组数据处理。");
        result.setRecommendProblems("");
        return result;
    }
}
