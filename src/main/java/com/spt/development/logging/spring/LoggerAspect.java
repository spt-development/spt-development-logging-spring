package com.spt.development.logging.spring;

import com.spt.development.cid.CorrelationId;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

abstract class LoggerAspect {
    private final boolean includeCorrelationIdInLogs;

    LoggerAspect(final boolean includeCorrelationIdInLogs) {
        this.includeCorrelationIdInLogs = includeCorrelationIdInLogs;
    }

    void trace(Logger logger, String format, Object... arguments) {
        log(logger::trace, format, arguments);
    }

    void debug(Logger logger, String format, Object... arguments) {
        log(logger::debug, format, arguments);
    }

    void info(Logger logger, String format, Object... arguments) {
        log(logger::info, format, arguments);
    }

    void error(Logger logger, String format, Object... arguments) {
        log(logger::error, format, arguments);
    }

    private void log(BiConsumer<String, Object[]> log, String format, Object[] arguments) {
        if (includeCorrelationIdInLogs) {
            log.accept("[{}] " + format, addCorrelationIdToArguments(arguments));
            return;
        }
        log.accept(format, arguments);
    }

    private Object[] addCorrelationIdToArguments(Object[] arguments) {
        final Object[] newArguments = new Object[arguments.length + 1];
        newArguments[0] = CorrelationId.get();

        System.arraycopy(arguments, 0, newArguments, 1, arguments.length);

        return newArguments;
    }
}
