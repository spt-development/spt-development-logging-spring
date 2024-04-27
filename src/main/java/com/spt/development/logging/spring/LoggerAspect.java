package com.spt.development.logging.spring;

import com.spt.development.cid.CorrelationId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.function.BiConsumer;

import static com.spt.development.logging.spring.LoggerUtil.formatArgs;

abstract class LoggerAspect {
    private final boolean includeCorrelationIdInLogs;
    private final boolean isStartAndCompleteMethodLoggedAtInfo;

    LoggerAspect(final boolean includeCorrelationIdInLogs) {
        this(includeCorrelationIdInLogs, false);
    }

    LoggerAspect(final boolean includeCorrelationIdInLogs, final boolean isStartAndCompleteMethodLoggedAtInfo) {
        this.includeCorrelationIdInLogs = includeCorrelationIdInLogs;
        this.isStartAndCompleteMethodLoggedAtInfo = isStartAndCompleteMethodLoggedAtInfo;
    }

    @Pointcut("@annotation(com.spt.development.logging.NoLogging) || @target(com.spt.development.logging.NoLogging)")
    void loggingDisabled() {}

    Object log(final ProceedingJoinPoint point) throws Throwable {
        final MethodSignature signature = (MethodSignature) point.getSignature();
        final Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        if (log.isEnabledForLevel(isStartAndCompleteMethodLoggedAtInfo ? Level.INFO : Level.DEBUG)) {
            startAndCompleteMethodLogger().accept(
                log, "{}.{}({})", point.getTarget().getClass().getSimpleName(), point.getSignature().getName(),
                formatArgs(signature.getMethod().getParameterAnnotations(), point.getArgs()));
        }
        return proceed(point, log);
    }

    Object proceed(ProceedingJoinPoint point, Logger log) throws Throwable {
        final Object result = point.proceed();

        if (log.isTraceEnabled()) {
            final MethodSignature methodSignature = (MethodSignature) point.getSignature();

            if (!methodSignature.getReturnType().equals(void.class)) {
                trace(log, "{}.{} Returned: {}", point.getTarget().getClass().getSimpleName(), methodSignature.getName(), result);

                return result;
            }
        }
        startAndCompleteMethodLogger().accept(log, "{}.{} - complete", point.getTarget().getClass().getSimpleName(), point.getSignature().getName());

        return result;
    }

    private LoggerConsumer startAndCompleteMethodLogger() {
        return isStartAndCompleteMethodLoggedAtInfo ? this::info : this::debug;
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

    @FunctionalInterface
    public interface LoggerConsumer {
        void accept(Logger log, String format, Object... arguments);
    }
}
