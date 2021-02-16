package com.spt.development.logging.spring;

import com.spt.development.cid.CorrelationId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import static com.spt.development.logging.spring.LoggerUtil.formatArgs;

/**
 * Logs calls to methods annotated with the <code>org.springframework.jms.annotation.JmsListener</code> annotation.
 */
@Aspect
@Order(1)
public class JmsListenerLogger {

    /**
     * Outputs INFO level logging when a public method annotated with the
     * <code>org.springframework.jms.annotation.JmsListener</code> annotation is called and when it returns (without
     * exception). For example:
     *
     * <pre>
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyListener.onMsg('40872057-a1b6-4fdd-bce1-7882929bbce6', '{"id":4,"eventTime":"2019-01-14T18:45:26Z"}')
     * ...
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyListener.onMsg - complete
     * </pre>
     *
     * @param point the aspect join point required for implementing a {@link Around} aspect.
     *
     * @return the value returned from the method logged.
     *
     * @throws Throwable thrown if the method logged throws a {@link Throwable}.
     */
    @Around("@annotation(org.springframework.jms.annotation.JmsListener)" +
            " && !@annotation(com.spt.development.logging.NoLogging)" +
            " && !@target(com.spt.development.logging.NoLogging)")
    public Object log(final ProceedingJoinPoint point) throws Throwable {
        final MethodSignature signature = (MethodSignature)point.getSignature();
        final Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        if (log.isInfoEnabled()) {

            log.info("[{}] {}.{}({})", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                    point.getSignature().getName(), formatArgs(signature.getMethod().getParameterAnnotations(), point.getArgs()));
        }
        return proceed(point, log);
    }

    private Object proceed(ProceedingJoinPoint point, Logger log) throws Throwable {
        final Object result = point.proceed();

        log.info("[{}] {}.{} - complete", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                point.getSignature().getName());

        return result;
    }
}
