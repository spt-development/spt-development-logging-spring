package com.spt.development.logging.spring;

import com.spt.development.cid.CorrelationId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.spt.development.logging.spring.LoggerUtil.formatArgs;

/**
 * Logs calls to all public methods belonging to classes with the
 * <code>org.springframework.web.bind.annotation.RestController</code> annotation.
 */
@Aspect
public class RestControllerLogger {

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
    @Around("@within(org.springframework.web.bind.annotation.RestController)" +
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

        try {
            final Object result = point.proceed();

            if (log.isTraceEnabled()) {
                final MethodSignature methodSignature = (MethodSignature)point.getSignature();

                if (!methodSignature.getReturnType().equals(void.class)) {
                    log.trace("[{}] {}.{} Returned: {}", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                            methodSignature.getName(), result);

                    return result;
                }
            }
            log.info("[{}] {}.{} - complete", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                    point.getSignature().getName());

            return result;
        }
        catch (Throwable t) {
            if (isUnexpectedOr5xxServerError(t)) {
                log.error("[{}] {}.{} threw exception: ", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                        point.getSignature().getName(), t);
            }
            else {
                log.info("[{}] {}.{} threw exception: {}", CorrelationId.get(), point.getTarget().getClass().getSimpleName(),
                        point.getSignature().getName(), t.getClass().getCanonicalName());

                log.debug("[{}] Exception: ", CorrelationId.get(), t);
            }
            throw t;
        }
    }

    private static final String HTTP_STATUS_FIELD = "code";

    private boolean isUnexpectedOr5xxServerError(Throwable t) {
        if (!(t instanceof HttpStatusCodeException)) {
            final AnnotationAttributes responseStatus = AnnotatedElementUtils.getMergedAnnotationAttributes(t.getClass(), ResponseStatus.class);

            return responseStatus == null || !responseStatus.containsKey(HTTP_STATUS_FIELD) ||
                    ((HttpStatus)responseStatus.getEnum(HTTP_STATUS_FIELD)).is5xxServerError();
        }
        return ((HttpStatusCodeException)t).getStatusCode().is5xxServerError();
    }
}
