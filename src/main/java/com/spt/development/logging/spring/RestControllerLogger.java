package com.spt.development.logging.spring;

import com.spt.development.logging.spring.invocation.LoggedInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Logs calls to all public methods belonging to classes with the
 * <code>org.springframework.web.bind.annotation.RestController</code> annotation.
 */
@Aspect
public class RestControllerLogger extends LoggerAspect {
    private static final String HTTP_STATUS_FIELD = "code";

    /**
     * Creates a new instance of the logger aspect. The log statements added by the aspect will include the current
     * correlation ID; see {@link RestControllerLogger#RestControllerLogger(boolean)} to disable this behaviour.
     */
    public RestControllerLogger() {
        this(true);
    }

    /**
     * Creates a new instance of the logger aspect.
     *
     * @param includeCorrelationIdInLogs a flag to determine whether the correlation ID should be explicitly included
     *                                   in the log statements added by the aspect.
     */
    public RestControllerLogger(final boolean includeCorrelationIdInLogs) {
        super(includeCorrelationIdInLogs, true);
    }

    /**
     * Outputs INFO level logging when a public method belonging to a class, annotated with the
     * <code>org.springframework.web.bind.annotation.RestController</code> annotation is called and when it returns (without
     * exception). If TRACE level logging is enabled and the method has a non-<code>void</code> return type, the
     * return value will be included in the logging. For example:
     *
     * <pre>
     * [c237922b-48a8-4451-860a-3b456a5ffe17] MyController.someActionMethod()
     * ...
     * [c237922b-48a8-4451-860a-3b456a5ffe17] MyController.someActionMethod Returned: &lt;200 OK OK,[]&gt;
     * </pre>
     *
     * <p>If an exception occurs, the exception is annotated with the {@link ResponseStatus} annotation and the exception
     * is mapped to a non-5XX HTTP status code, the exception is logged at INFO level (the full exception details are
     * logged at DEBUG), otherwise i.e. if the exception is mapped to a 5XX status code, the full exception details
     * including stack trace are logged at ERROR.</p>
     *
     * @param point the aspect join point required for implementing a {@link Around} aspect.
     *
     * @return the value returned from the method logged.
     *
     * @throws Throwable thrown if the method logged throws a {@link Throwable}.
     */
    @Override
    @Around("@within(org.springframework.web.bind.annotation.RestController) && !loggingDisabled()")
    public Object log(final ProceedingJoinPoint point) throws Throwable {
        return super.log(point);
    }

    @Override
    Object proceed(LoggedInvocation invocation, Logger log) throws Throwable {
        try {
            return super.proceed(invocation, log);
        } catch (Throwable t) {
            if (isUnexpectedOr5xxServerError(t)) {
                error(log, "{}.{} threw exception: ", invocation.getDeclaringClass().getSimpleName(), invocation.getMethod().getName(), t);
            } else {
                info(log, "{}.{} threw exception: {}", invocation.getDeclaringClass().getSimpleName(), invocation.getMethod().getName(),
                        t.getClass().getCanonicalName());

                debug(log, "Exception: ", t);
            }
            throw t;
        }
    }

    private boolean isUnexpectedOr5xxServerError(Throwable t) {
        if (!(t instanceof HttpStatusCodeException)) {
            final AnnotationAttributes responseStatus = AnnotatedElementUtils.getMergedAnnotationAttributes(t.getClass(), ResponseStatus.class);

            return responseStatus == null || !responseStatus.containsKey(HTTP_STATUS_FIELD)
                || ((HttpStatus) responseStatus.getEnum(HTTP_STATUS_FIELD)).is5xxServerError();
        }
        return ((HttpStatusCodeException) t).getStatusCode().is5xxServerError();
    }
}
