package com.agentedu.enums;

public enum QuestionTypeEnum {
    PROGRAMMING,
    CHOICE,
    FILL_BLANK,
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
