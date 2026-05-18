package com.agentedu.enums;

public enum UserRoleEnum {
    STUDENT,
    TEACHER;

    /**
     * 判断传入角色是否为系统支持的角色。
     */
    public static boolean isValid(String role) {
        if (role == null) {
            return false;
        }
        for (UserRoleEnum value : values()) {
            if (value.name().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
