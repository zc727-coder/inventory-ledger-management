package cn.sycoder.utils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class LogMaskUtils {

    private LogMaskUtils() {
    }

    public static String maskArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .map(LogMaskUtils::maskArg)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String maskArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
            return arg.getClass().getSimpleName();
        }

        String text = String.valueOf(arg);
        String lower = text.toLowerCase(Locale.ROOT);

        if (lower.contains("password=")) {
            text = text.replaceAll("(?i)password=([^,}\\]]+)", "password=***");
        }
        if (lower.contains("token=")) {
            text = text.replaceAll("(?i)token=([^,}\\]]+)", "token=***");
        }

        return text;
    }
}
