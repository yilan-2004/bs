package com.agentedu.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class ProcessUtils {

    private ProcessUtils() {
    }

    public static boolean waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        boolean finished = process.waitFor(timeout, unit);
        if (!finished) {
            process.destroyForcibly();
        }
        return finished;
    }

    /**
     * 流式读取有限输出，避免 readAllBytes 在异常大输出时占用过多内存。
     */
    public static String readLimited(InputStream inputStream, int maxLength) throws IOException {
        int limit = Math.max(maxLength, 0);
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(limit, 8192));
        byte[] buffer = new byte[1024];
        int total = 0;
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            int remaining = limit - total;
            if (remaining > 0) {
                int copyLength = Math.min(read, remaining);
                output.write(buffer, 0, copyLength);
                total += copyLength;
            }
        }
        byte[] bytes = output.toByteArray();
        String text = new String(bytes, StandardCharsets.UTF_8);
        return total >= limit && limit > 0 ? text + "\n[output truncated]" : text;
    }
}
