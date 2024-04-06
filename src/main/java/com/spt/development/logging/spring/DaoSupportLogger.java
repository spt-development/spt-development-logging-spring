package com.spt.development.logging.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Logs calls to all public methods belonging to classes with the <code>org.springframework.stereotype.Repository</code>
 * annotation.
 */
@Aspect
public class DaoSupportLogger extends LoggerAspect {

    /**
     * Creates a new instance of the logger aspect. The log statements added by the aspect will include the current
     * correlation ID; see {@link DaoSupportLogger#DaoSupportLogger(boolean)} to disable this behaviour.
     */
    public DaoSupportLogger() {
        this(true);
    }

    /**
     * Creates a new instance of the logger aspect.
     *
     * @param includeCorrelationIdInLogs a flag to determine whether the correlation ID should be explicitly included
     *                                   in the log statements added by the aspect.
     */
    public DaoSupportLogger(final boolean includeCorrelationIdInLogs) {
        super(includeCorrelationIdInLogs);
    }

    /**
     * Outputs DEBUG level logging when a public method belonging to a class, extending
     * <code>org.springframework.dao.support.DaoSupport</code> is called and when it returns (without
     * exception). If TRACE level logging is enabled and the method has a non-<code>void</code> return type, the
     * return value will be included in the logging. For example:
     *
     * <pre>
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyDao.read(4)
     * ...
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyDao.read Returned: MyEntity(id=4, name=test)
     * </pre>
     *
     * @param point the aspect join point required for implementing a {@link Around} aspect.
     *
     * @return the value returned from the method logged.
     *
     * @throws Throwable thrown if the method logged throws a {@link Throwable}.
     */
    @Override
    @Around("execution(public * org.springframework.dao.support.DaoSupport+.*(..)) && !loggingDisabled()")
    public Object log(final ProceedingJoinPoint point) throws Throwable {
        return super.log(point);
    }
}
