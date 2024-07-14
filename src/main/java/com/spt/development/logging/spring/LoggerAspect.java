package com.spt.development.logging.spring;

import com.spt.development.logging.spring.invocation.ProceedingJoinPointAdapter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Pointcut;

import static com.spt.development.logging.spring.LoggerUtil.LOGGING_DISABLED_POINTCUT_EXPRESSION;

abstract class LoggerAspect extends InvocationLogger {
    LoggerAspect(final boolean includeCorrelationIdInLogs) {
        this(includeCorrelationIdInLogs, false);
    }

    LoggerAspect(final boolean includeCorrelationIdInLogs, final boolean isStartAndCompleteMethodLoggedAtInfo) {
        super(includeCorrelationIdInLogs, isStartAndCompleteMethodLoggedAtInfo);
    }

    @Pointcut(LOGGING_DISABLED_POINTCUT_EXPRESSION)
    void loggingDisabled() {}

    Object log(final ProceedingJoinPoint point) throws Throwable {
        return log(new ProceedingJoinPointAdapter(point));
    }
}
