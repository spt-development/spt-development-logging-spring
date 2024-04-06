package com.spt.development.logging.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.core.annotation.Order;

/**
 * Logs calls to methods annotated with the <code>org.springframework.jms.annotation.JmsListener</code> annotation.
 */
@Aspect
@Order(1)
public class JmsListenerLogger extends LoggerAspect {

    /**
     * Creates a new instance of the logger aspect. The log statements added by the aspect will include the current
     * correlation ID; see {@link JmsListenerLogger#JmsListenerLogger(boolean)} to disable this behaviour.
     */
    public JmsListenerLogger() {
        this(true);
    }

    /**
     * Creates a new instance of the logger aspect.
     *
     * @param includeCorrelationIdInLogs a flag to determine whether the correlation ID should be explicitly included
     *                                   in the log statements added by the aspect.
     */
    public JmsListenerLogger(final boolean includeCorrelationIdInLogs) {
        super(includeCorrelationIdInLogs, true);
    }

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
    @Override
    @Around("@annotation(org.springframework.jms.annotation.JmsListener) && !loggingDisabled()")
    public Object log(final ProceedingJoinPoint point) throws Throwable {
        return super.log(point);
    }

    @Override
    Object proceed(ProceedingJoinPoint point, Logger log) throws Throwable {
        final Object result = point.proceed();

        info(log, "{}.{} - complete", point.getTarget().getClass().getSimpleName(), point.getSignature().getName());

        return result;
    }
}
