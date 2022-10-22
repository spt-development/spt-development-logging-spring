package com.spt.development.logging.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spt.development.logging.spring.LoggerUtil.formatArgs;

/**
 * Logs calls to all public methods belonging to classes with the <code>org.springframework.stereotype.Repository</code>
 * annotation.
 */
@Aspect
public class RepositoryLogger extends LoggerAspect {

    /**
     * Creates a new instance of the logger aspect. The log statements added by the aspect will include the current
     * correlation ID; see {@link RepositoryLogger#RepositoryLogger(boolean)} to disable this behaviour.
     */
    public RepositoryLogger() {
        this(true);
    }

    /**
     * Creates a new instance of the logger aspect.
     *
     * @param includeCorrelationIdInLogs a flag to determine whether the correlation ID should be explicitly included
     *                                   in the log statements added by the aspect.
     */
    public RepositoryLogger(final boolean includeCorrelationIdInLogs) {
        super(includeCorrelationIdInLogs);
    }

    /**
     * Outputs DEBUG level logging when a public method belonging to a class, annotated with the
     * <code>org.springframework.stereotype.Repository</code> annotation is called and when it returns (without
     * exception). If TRACE level logging is enabled and the method has a non-<code>void</code> return type, the
     * return value will be included in the logging. For example:
     *
     * <pre>
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyRepository.read(4)
     * ...
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyRepository.read Returned: MyEntity(id=4, name=test)
     * </pre>
     *
     * @param point the aspect join point required for implementing a {@link Around} aspect.
     *
     * @return the value returned from the method logged.
     *
     * @throws Throwable thrown if the method logged throws a {@link Throwable}.
     */
    @Around("@within(org.springframework.stereotype.Repository)" +
            " && !@annotation(com.spt.development.logging.NoLogging)" +
            " && !@target(com.spt.development.logging.NoLogging)")
    public Object log(final ProceedingJoinPoint point) throws Throwable {
        final MethodSignature signature = (MethodSignature)point.getSignature();
        final Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        if (log.isDebugEnabled()) {
            debug(log, "{}.{}({})", point.getTarget().getClass().getSimpleName(), point.getSignature().getName(),
                    formatArgs(signature.getMethod().getParameterAnnotations(), point.getArgs()));
        }
        return proceed(point, log);
    }

    private Object proceed(ProceedingJoinPoint point, Logger log) throws Throwable {
        final Object result = point.proceed();

        if (log.isTraceEnabled()) {
            final MethodSignature methodSignature = (MethodSignature)point.getSignature();

            if (!methodSignature.getReturnType().equals(void.class)) {
                trace(log, "{}.{} Returned: {}", point.getTarget().getClass().getSimpleName(), methodSignature.getName(), result);

                return result;
            }
        }
        debug(log, "{}.{} - complete", point.getTarget().getClass().getSimpleName(), point.getSignature().getName());

        return result;
    }
}
