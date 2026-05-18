package com.agentedu.service.impl;

import com.agentedu.enums.JudgeStatusEnum;
import com.agentedu.service.AiModelClient;
import com.agentedu.service.agent.AgentFeedbackResult;
import com.agentedu.service.agent.CodeContext;

public class MockAiModelClient implements AiModelClient {

    /**
     * 根据评测状态返回模拟 AI 反馈，不调用真实外部模型。
     */
    @Override
    public AgentFeedbackResult chat(CodeContext context) {
        String status = context.getJudgeStatus();
        if (JudgeStatusEnum.ACCEPTED.name().equals(status)) {
            return AgentFeedbackResult.accepted();
        }

        AgentFeedbackResult result = new AgentFeedbackResult();
        result.setRecommendProblems("");

        if ("SHORT_ANSWER".equals(context.getQuestionType())) {
            result.setScore(context.getStudentAnswer() != null && context.getStudentAnswer().length() >= 20 ? 75 : 45);
            result.setErrorType(result.getScore() >= 60 ? "概念理解基本到位但表述不完整" : "概念理解不完整");
            result.setDiagnosis("该简答题反馈由 Mock AI 生成。你的回答已经覆盖部分要点，但与参考答案和评分要点相比，仍需要补充关键概念、因果关系或必要例子。");
            result.setExplanation("简答题不仅要求给出结论，还要体现对核心概念、适用条件和关键理由的理解。评分会关注答案是否覆盖题干要求、知识点是否准确、表达是否完整。");
            result.setSuggestion("建议对照题干逐项检查：是否回答了核心问题，是否说明了原因，是否遗漏评分要点。修改时补充关键概念和必要解释，不要只堆关键词。");
            result.setEvaluation(result.getScore() >= 60
                    ? "整体方向是对的，继续完善表达结构和关键细节，可以获得更稳定的得分。"
                    : "当前答案还比较粗略，先回到相关知识点重新梳理概念，再尝试用自己的话补充完整。");
            result.setRelatedKnowledge(context.getKnowledgeTags());
            result.setNextPracticeAdvice("继续练习同知识点的简答题，尝试使用“概念说明 + 原因解释 + 例子或条件”的结构作答。");
            return result;
        }

        if (JudgeStatusEnum.WRONG_ANSWER.name().equals(status)) {
            result.setErrorType("逻辑错误");
            result.setDiagnosis("程序可以运行，但实际输出与期望输出不一致。请结合失败用例检查核心计算逻辑，尤其是运算符、边界条件和输出格式。");
            result.setExplanation("Wrong Answer 通常说明语法没有问题，但算法思路或实现细节与题意不完全一致。失败用例能帮助你定位哪一类输入会暴露问题。");
            result.setSuggestion("建议先手动代入失败输入，分别写出期望输出和你的代码输出，再检查核心表达式或条件分支。不建议直接重写整段代码。");
            result.setEvaluation("你已经完成了可运行代码，下一步重点是训练用测试用例反推逻辑偏差的能力。");
            result.setRelatedKnowledge("逻辑表达式、边界条件、测试用例分析");
            result.setNextPracticeAdvice("继续练习同类基础题，并尝试为每道题补充 2 到 3 个边界测试。");
            return result;
        }
        if (JudgeStatusEnum.RUNTIME_ERROR.name().equals(status)) {
            result.setErrorType("运行时错误");
            result.setDiagnosis("程序在运行过程中出现异常，可能与变量未定义、输入格式解析、数组下标或类型转换有关。");
            result.setExplanation("Runtime Error 表示代码语法基本可执行，但某些输入触发了异常。需要结合错误信息定位异常类型和出错位置。");
            result.setSuggestion("建议检查变量是否已定义、输入格式是否匹配、列表访问是否越界，以及是否存在除零等特殊情况。");
            result.setEvaluation("你已经进入调试阶段，学会阅读错误栈和异常类型会明显提升修复效率。");
            result.setRelatedKnowledge("异常调试、变量作用域、输入解析、列表下标");
            result.setNextPracticeAdvice("练习包含不同输入格式和边界数据的题目，重点训练异常信息定位能力。");
            return result;
        }
        if (JudgeStatusEnum.COMPILE_ERROR.name().equals(status)) {
            result.setErrorType("语法错误");
            result.setDiagnosis("代码在执行前出现语法或缩进问题，Python 解释器无法正常运行该程序。");
            result.setExplanation("Compile Error 常见原因包括括号不匹配、缩进错误、缺少冒号、表达式不完整等。");
            result.setSuggestion("建议优先检查报错行及其上一行附近的括号、缩进、冒号和表达式完整性。");
            result.setEvaluation("先把代码调整到可运行状态，再继续验证算法逻辑，这是很好的调试顺序。");
            result.setRelatedKnowledge("Python语法、缩进规则、表达式完整性");
            result.setNextPracticeAdvice("多练习短代码片段的语法检查，养成运行前检查括号、冒号和缩进的习惯。");
            return result;
        }
        if (JudgeStatusEnum.TIME_LIMIT_EXCEEDED.name().equals(status)) {
            result.setErrorType("运行超时");
            result.setDiagnosis("程序在规定时间内没有结束，可能存在死循环、递归无法收敛，或算法复杂度过高。");
            result.setExplanation("Time Limit Exceeded 说明程序没有及时给出结果，需要关注循环退出条件、递归出口和整体复杂度。");
            result.setSuggestion("建议检查是否存在死循环，重点看 while/for 循环的退出条件、递归终止条件，并思考是否需要更高效的算法。");
            result.setEvaluation("你需要进一步提升复杂度分析能力，避免代码在较大输入下超时。");
            result.setRelatedKnowledge("循环条件、递归出口、时间复杂度");
            result.setNextPracticeAdvice("练习循环和递归相关题目，重点标注每个循环变量如何变化、何时结束。");
            return result;
        }

        result.setErrorType("综合错误");
        result.setDiagnosis("本次提交未通过，但当前 Mock AI 无法进一步细分错误类型。");
        result.setExplanation("可以先查看评测状态、错误输出和失败测试用例，确定问题属于语法、运行、逻辑还是性能方向。");
        result.setSuggestion("建议从失败输入开始手动调试，逐步缩小问题范围。");
        result.setEvaluation("继续保持调试意识，逐步定位问题。");
        result.setRelatedKnowledge("调试方法、测试用例分析");
        result.setNextPracticeAdvice("选择同知识点基础题继续练习，并记录每次错误原因。");
        return result;
    }
}
