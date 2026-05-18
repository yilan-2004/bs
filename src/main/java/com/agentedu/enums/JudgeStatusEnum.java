package com.agentedu.enums;

public enum JudgeStatusEnum {
    JUDGING("Judging"),
    ACCEPTED("Accepted"),
    WRONG_ANSWER("Wrong Answer"),
    PARTIAL_ACCEPTED("Partial Accepted"),
    RUNTIME_ERROR("Runtime Error"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"),
    COMPILE_ERROR("Compile Error"),
    AI_EVALUATE_FAILED("AI Evaluate Failed"),
    SYSTEM_ERROR("System Error");

    private final String desc;

    JudgeStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
