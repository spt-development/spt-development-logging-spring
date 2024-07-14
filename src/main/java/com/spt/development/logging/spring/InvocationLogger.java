package com.spt.development.logging.spring;

import com.spt.development.cid.CorrelationId;
import com.spt.development.logging.spring.invocation.LoggedInvocation;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import static com.spt.development.logging.spring.LoggerUtil.formatArgs;

abstract class InvocationLogger {

    private final boolean includeCorrelationIdInLogs;
    private final boolean isStartAndCompleteMethodLoggedAtInfo;

    InvocationLogger(final boolean includeCorrelationIdInLogs) {
        this(includeCorrelationIdInLogs, false);
    }

    InvocationLogger(final boolean includeCorrelationIdInLogs, final boolean isStartAndCompleteMethodLoggedAtInfo) {
        this.includeCorrelationIdInLogs = includeCorrelationIdInLogs;
        this.isStartAndCompleteMethodLoggedAtInfo = isStartAndCompleteMethodLoggedAtInfo;
    }

    Object log(final LoggedInvocation invocation) throws Throwable {
        final Method method = invocation.getMethod();
        final org.slf4j.Logger log = LoggerFactory.getLogger(invocation.getDeclaringClass());

        if (log.isEnabledForLevel(isStartAndCompleteMethodLoggedAtInfo ? Level.INFO : Level.DEBUG)) {
            startAndCompleteMethodLogger().accept(
                log, "{}.{}({})", invocation.getDeclaringClass().getSimpleName(), method.getName(),
                formatArgs(method.getParameterAnnotations(), invocation.getArgs()));
        }
        return proceed(invocation, log);
    }

    Object proceed(LoggedInvocation invocation, org.slf4j.Logger log) throws Throwable {
        final Object result = invocation.proceed();

        if (log.isTraceEnabled()) {
            final Method method = invocation.getMethod();

            if (!method.getReturnType().equals(void.class)) {
                trace(log, "{}.{} Returned: {}", invocation.getDeclaringClass().getSimpleName(), method.getName(), result);

                return result;
            }
        }

        startAndCompleteMethodLogger().accept(
            log, "{}.{} - complete",
            invocation.getDeclaringClass().getSimpleName(),
            invocation.getMethod().getName()
        );
        return result;
    }

    private LoggerConsumer startAndCompleteMethodLogger() {
        return isStartAndCompleteMethodLoggedAtInfo ? this::info : this::debug;
    }

    void trace(org.slf4j.Logger logger, String format, Object... arguments) {
        log(logger::trace, format, arguments);
    }

    void debug(org.slf4j.Logger logger, String format, Object... arguments) {
        log(logger::debug, format, arguments);
    }

    void info(org.slf4j.Logger logger, String format, Object... arguments) {
        log(logger::info, format, arguments);
    }

    void error(org.slf4j.Logger logger, String format, Object... arguments) {
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

    @FunctionalInterface
    public interface LoggerConsumer {
        void accept(org.slf4j.Logger log, String format, Object... arguments);
    }
}
