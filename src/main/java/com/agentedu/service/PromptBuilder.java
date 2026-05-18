package com.agentedu.service;

import com.agentedu.service.agent.CodeContext;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    /**
     * 构建稳定的教学场景系统提示词。所有模型输出必须是中文 JSON。
     */
    public String buildSystemPrompt() {
        return """
                你是高校多学科个性化学习平台 AgentEdu 的 AI 学习助教。
                你必须遵守：
                1. 所有字段内容必须使用简体中文，不允许输出英文解释。
                2. 只返回一个合法 JSON 对象，不要 Markdown，不要代码块，不要额外说明。
                3. 不要直接给出完整答案作为开头。
                4. 不要替学生完整改写代码或完整代写答案。
                5. 重点说明错误原因、相关知识点、修改方向、学习评价和下一步练习建议。
                6. 如果提供了失败用例、学生答案、参考答案或知识库证据，必须结合这些信息解释问题。
                7. 语气要像高校课程助教：清晰、耐心、鼓励，但不要空泛。

                必须严格返回以下 JSON 字段：
                {
                  "score": 0,
                  "errorType": "中文错误类型",
                  "diagnosis": "中文错因诊断",
                  "explanation": "中文知识讲解",
                  "suggestion": "中文修改建议",
                  "evaluation": "中文学习评价",
                  "relatedKnowledge": "中文相关知识点",
                  "nextPracticeAdvice": "中文下一步练习建议"
                }
                """;
    }

    /**
     * 根据题型构建用户提示词。
     */
    public String buildUserPrompt(CodeContext context) {
        if ("GENERAL_TUTOR".equals(context.getQuestionType())) {
            return buildGeneralTutorPrompt(context);
        }
        if ("SHORT_ANSWER".equals(context.getQuestionType())) {
            return buildShortAnswerPrompt(context);
        }
        if (!"PROGRAMMING".equals(context.getQuestionType())) {
            return buildObjectiveQuestionPrompt(context);
        }
        return buildProgrammingPrompt(context);
    }

    private String buildGeneralTutorPrompt(CodeContext context) {
        return """
                请回答学生在 AI 助教页面提出的学习问题。

                学生问题：
                %s

                回答要求：
                1. 必须使用简体中文。
                2. 不要只给结论，要先解释思路，再给可执行建议。
                3. 如果问题和编程学习、错题复盘、知识点理解有关，请给出分步骤学习方法。
                4. 不要生成与学习无关的内容。
                5. 返回 JSON，字段必须完整。
                """.formatted(nullToEmpty(context.getProblemDescription()));
    }

    private String buildProgrammingPrompt(CodeContext context) {
        return """
                请分析这次编程题提交，并生成中文教学反馈。

                提交ID：%s
                题目ID：%s
                题目标题：%s
                题目描述：%s
                知识点：%s
                评测状态：%s
                失败输入：%s
                期望输出：%s
                实际输出：%s
                错误信息：%s
                课程知识库证据：
                %s
                关键代码片段：
                ```python
                %s
                ```

                请结合失败用例解释为什么错，指出涉及的知识点和修改方向。
                不要直接给出完整正确代码。必须返回简体中文 JSON。
                """.formatted(
                nullToEmpty(context.getSubmitId()),
                nullToEmpty(context.getProblemId()),
                nullToEmpty(context.getProblemTitle()),
                nullToEmpty(context.getProblemDescription()),
                nullToEmpty(context.getKnowledgeTags()),
                nullToEmpty(context.getJudgeStatus()),
                nullToEmpty(context.getFailedInput()),
                nullToEmpty(context.getExpectedOutput()),
                nullToEmpty(context.getActualOutput()),
                nullToEmpty(context.getErrorMessage()),
                evidenceBlock(context),
                nullToEmpty(context.getKeyCodeSnippet())
        );
    }

    private String buildObjectiveQuestionPrompt(CodeContext context) {
        return """
                请分析这道客观题或填空题的作答，并生成中文教学反馈。

                提交ID：%s
                题目ID：%s
                题型：%s
                题目标题：%s
                题目描述：%s
                知识点：%s
                评测状态：%s
                学生答案：%s
                正确答案：%s
                错误信息：%s
                课程知识库证据：
                %s

                请先解释相关概念和错误原因，再给修改方向。
                不要把完整答案作为第一句话。必须返回简体中文 JSON。
                """.formatted(
                nullToEmpty(context.getSubmitId()),
                nullToEmpty(context.getProblemId()),
                nullToEmpty(context.getQuestionType()),
                nullToEmpty(context.getProblemTitle()),
                nullToEmpty(context.getProblemDescription()),
                nullToEmpty(context.getKnowledgeTags()),
                nullToEmpty(context.getJudgeStatus()),
                nullToEmpty(context.getStudentAnswer()),
                nullToEmpty(context.getCorrectAnswer()),
                nullToEmpty(context.getErrorMessage()),
                evidenceBlock(context)
        );
    }

    private String buildShortAnswerPrompt(CodeContext context) {
        return """
                请批改这道高校课程简答题。必须只返回一个合法 JSON 对象。
                不要直接展示参考答案，不要替学生完整重写答案。

                提交ID：%s
                题目ID：%s
                题型：SHORT_ANSWER
                题目标题：%s
                题干：%s
                知识点：%s
                参考答案：%s
                评分要点：%s
                满分：%s
                学生答案：%s
                课程知识库证据：
                %s

                请给出 0-100 的 score，并用简体中文填写：
                errorType、diagnosis、explanation、suggestion、evaluation、relatedKnowledge、nextPracticeAdvice。
                """.formatted(
                nullToEmpty(context.getSubmitId()),
                nullToEmpty(context.getProblemId()),
                nullToEmpty(context.getProblemTitle()),
                nullToEmpty(context.getProblemDescription()),
                nullToEmpty(context.getKnowledgeTags()),
                nullToEmpty(context.getCorrectAnswer()),
                nullToEmpty(context.getScoringPoints()),
                nullToEmpty(context.getMaxScore()),
                nullToEmpty(context.getStudentAnswer()),
                evidenceBlock(context)
        );
    }

    private String evidenceBlock(CodeContext context) {
        if (context == null || !Boolean.TRUE.equals(context.getRagUsed())) {
            return "未检索到课程知识库证据。";
        }
        return """
                请优先依据以下教师维护的课程知识片段：
                证据片段ID：%s
                证据摘要：
                %s

                证据正文：
                %s
                """.formatted(
                nullToEmpty(context.getEvidenceChunkIds()),
                nullToEmpty(context.getEvidenceSummary()),
                nullToEmpty(context.getEvidenceText())
        );
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
