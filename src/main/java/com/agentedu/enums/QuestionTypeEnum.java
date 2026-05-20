package com.agentedu.enums;

public enum QuestionTypeEnum {
    PROGRAMMING,
    CHOICE,
    MULTI_CHOICE,
    FILL_BLANK,
    TRUE_FALSE,
    SHORT_ANSWER;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (QuestionTypeEnum item : values()) {
            if (item.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
