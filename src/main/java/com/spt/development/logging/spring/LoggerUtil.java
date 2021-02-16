package com.spt.development.logging.spring;

import com.spt.development.logging.NoLogging;

import java.lang.annotation.Annotation;
import java.util.Arrays;

final class LoggerUtil {
    static final int MAX_DEBUG_STR_ARG_LEN = 75;

    static final String MASKED_ARG = "******";

    private LoggerUtil() {}

    static String formatArgs(Annotation[][] annotations, Object[] args) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < annotations.length; i++) {
            sb.append(isNotToBeLogged(annotations[i]) ? MASKED_ARG : strValueOf(args[i]));

            if (i < annotations.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static boolean isNotToBeLogged(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .anyMatch(a -> NoLogging.class.isAssignableFrom(a.getClass()));
    }

    private static String strValueOf(Object obj) {
        if (obj instanceof String) {
            String strArg = obj.toString();

            if (strArg.length() > MAX_DEBUG_STR_ARG_LEN) {
                strArg = String.format("%s...", strArg.substring(0, MAX_DEBUG_STR_ARG_LEN - 3));
            }
            strArg = strArg.replaceAll("(\r\n|\r|\n)", "\\\\n");
            strArg = strArg.replaceAll("'", "\\'");

            return String.format("'%s'", strArg);
        }
        return String.valueOf(obj);
    }
}
