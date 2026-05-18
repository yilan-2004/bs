package com.agentedu.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.enums.UserRoleEnum;
import com.agentedu.exception.BusinessException;

public final class RoleAuthUtils {

    private static final String ROLE_KEY = "role";

    private RoleAuthUtils() {
    }

    /**
     * 获取当前登录用户角色，角色在登录成功后写入Sa-Token会话。
     */
    public static String getCurrentRole() {
        StpUtil.checkLogin();
        return StpUtil.getSession().getString(ROLE_KEY);
    }

    /**
     * 判断当前登录用户是否为教师。
     */
    public static boolean isTeacher() {
        return UserRoleEnum.TEACHER.name().equals(getCurrentRole());
    }

    /**
     * 判断当前登录用户是否为学生。
     */
    public static boolean isStudent() {
        return UserRoleEnum.STUDENT.name().equals(getCurrentRole());
    }

    /**
     * 要求当前用户必须是教师，否则抛出业务异常。
     */
    public static void requireTeacher() {
        if (!isTeacher()) {
            throw new BusinessException("仅教师可以执行该操作");
        }
    }

    /**
     * 要求当前用户必须是学生，否则抛出业务异常。
     */
    public static void requireStudent() {
        if (!isStudent()) {
            throw new BusinessException("仅学生可以执行该操作");
        }
    }
}
