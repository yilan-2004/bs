package com.agentedu.utils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class CodeSecurityUtils {

    private static final List<Pattern> DANGEROUS_PATTERNS = List.of(
            Pattern.compile("(^|\\n)\\s*(from|import)\\s+(os|sys|io|subprocess|socket|requests|shutil|pathlib|ctypes|multiprocessing|threading|signal|resource|importlib|builtins|pickle|marshal|runpy|http|urllib|ftplib|telnetlib)\\b"),
            Pattern.compile("\\b(__import__|eval|exec|open|compile|globals\\s*\\(|locals\\s*\\(|vars\\s*\\(|dir\\s*\\(|getattr\\s*\\(|setattr\\s*\\(|delattr\\s*\\()"),
            Pattern.compile("\\b(os\\.|sys\\.|subprocess\\.|socket\\.|shutil\\.|pathlib\\.|ctypes\\.|importlib\\.)"),
            Pattern.compile("\\b(__class__|__bases__|__subclasses__|__globals__|__builtins__|__dict__|__mro__)\\b")
    );

    private CodeSecurityUtils() {
    }

    public static boolean containsDangerousKeyword(String code) {
        return findDangerousKeyword(code) != null;
    }

    /**
     * 返回命中的危险模式。当前仍是静态过滤，不能替代真正的容器沙箱。
     */
    public static String findDangerousKeyword(String code) {
        if (code == null) {
            return null;
        }
        String normalized = stripComments(code).toLowerCase(Locale.ROOT);
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(normalized).find()) {
                return pattern.pattern();
            }
        }
        return null;
    }

    private static String stripComments(String code) {
        StringBuilder builder = new StringBuilder();
        for (String line : code.split("\\R", -1)) {
            int index = line.indexOf('#');
            builder.append(index >= 0 ? line.substring(0, index) : line).append('\n');
        }
        return builder.toString();
    }
}
