package com.spt.development.logging.spring;

import com.spt.development.cid.CorrelationId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.spt.development.logging.spring.LoggerUtil.formatArgs;

/**
 * Logs calls to all public methods belonging to classes with the <code>org.springframework.stereotype.Service</code>
 * annotation.
 */
@Aspect
public class ServiceLogger {

    /**
     * Outputs DEBUG level logging when a public method belonging to a class, annotated with the
     * <code>org.springframework.stereotype.Service</code> annotation is called and when it returns (without
     * exception). If TRACE level logging is enabled and the method has a non-<code>void</code> return type, the
     * return value will be included in the logging. For example:
     *
     * <pre>
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyService.read(4)
     * ...
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyService.read Returned: MyEntity(id=4, name=test)
     * </pre>
     *
     * @param point the aspect join point required for implementing a {@link Around} aspect.
     *
     * @return the value returned from the method logged.
     *
     * @throws Throwable thrown if the method logged throws a {@link Throwable}.
     */
    @Around("@within(org.springframework.stereotype.Service)" +
            " && !@annotation(com.spt.development.logging.NoLogging)" +
            " && !@target(com.spt.development.logging.NoLogging)")
    public Object log(final ProceedingJoinPoint point) throws Throwable {
        final MethodSignature signature = (MethodSignature)point.getSignature();
        final Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        if (log.isDebugEnabled()) {
            log.debug("[{}] {}.{}({})", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                    point.getSignature().getName(), formatArgs(signature.getMethod().getParameterAnnotations(), point.getArgs()));
        }
        return proceed(point, log);
    }

    private Object proceed(ProceedingJoinPoint point, Logger log) throws Throwable {
        final Object result = point.proceed();

        if (log.isTraceEnabled()) {
            final MethodSignature methodSignature = (MethodSignature)point.getSignature();

            if (!methodSignature.getReturnType().equals(void.class)) {
                log.trace("[{}] {}.{} Returned: {}", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                        methodSignature.getName(), result);

                return result;
            }
        }
        log.debug("[{}] {}.{} - complete", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                point.getSignature().getName());

        return result;
    }
}
