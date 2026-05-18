package com.agentedu.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class PasswordUtils {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private PasswordUtils() {
    }

    /**
     * 使用BCrypt加密原始密码。
     */
    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 使用BCrypt校验原始密码和密文是否匹配。
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
