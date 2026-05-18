package com.agentedu.enums;

public enum ProblemDifficultyEnum {
    EASY,
    MEDIUM,
    HARD;

    /**
     * 判断传入难度是否为系统支持的题目难度。
     */
    public static boolean isValid(String difficulty) {
        if (difficulty == null) {
            return false;
        }
        for (ProblemDifficultyEnum value : values()) {
            if (value.name().equals(difficulty)) {
                return true;
            }
        }
        return false;
    }
}
