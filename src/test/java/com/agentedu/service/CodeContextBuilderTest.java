package com.agentedu.service;

import com.agentedu.entity.Problem;
import com.agentedu.entity.SubmitCaseResult;
import com.agentedu.entity.SubmitRecord;
import com.agentedu.service.agent.CodeContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeContextBuilderTest {

    @Test
    void buildShouldIncludeProblemFailedCaseErrorAndSnippet() {
        CodeContextBuilder builder = new CodeContextBuilder();
        SubmitRecord submitRecord = new SubmitRecord();
        submitRecord.setId(10L);
        submitRecord.setProblemId(20L);
        submitRecord.setJudgeStatus("WRONG_ANSWER");
        submitRecord.setErrorMessage("output mismatch");
        submitRecord.setCode("a, b = map(int, input().split())\nprint(a - b)");

        Problem problem = new Problem();
        problem.setId(20L);
        problem.setTitle("Sum A B");
        problem.setDescription("Read two integers and output their sum.");
        problem.setKnowledgeTags("basic,io");

        SubmitCaseResult failedCase = new SubmitCaseResult();
        failedCase.setInputData("1 2");
        failedCase.setExpectedOutput("3");
        failedCase.setActualOutput("-1");
        failedCase.setErrorOutput("");

        CodeContext context = builder.build(submitRecord, problem, failedCase);

        assertThat(context.getSubmitId()).isEqualTo(10L);
        assertThat(context.getProblemId()).isEqualTo(20L);
        assertThat(context.getProblemTitle()).isEqualTo("Sum A B");
        assertThat(context.getProblemDescription()).contains("output their sum");
        assertThat(context.getKnowledgeTags()).isEqualTo("basic,io");
        assertThat(context.getJudgeStatus()).isEqualTo("WRONG_ANSWER");
        assertThat(context.getFailedInput()).isEqualTo("1 2");
        assertThat(context.getExpectedOutput()).isEqualTo("3");
        assertThat(context.getActualOutput()).isEqualTo("-1");
        assertThat(context.getErrorMessage()).isEqualTo("output mismatch");
        assertThat(context.getKeyCodeSnippet()).contains("print(a - b)");
    }
}
