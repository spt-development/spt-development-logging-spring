package com.spt.development.logging.spring;

import com.spt.development.logging.spring.invocation.MethodInvocationAdapter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Logs calls to methods. Intended to be combined with {@link org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor}
 * to add logging to the public methods of arbitrary Spring beans.
 */
public class BeanLogger extends InvocationLogger implements MethodInterceptor {

    /**
     * Creates a new instance of the logger method interceptor. The log statements added by the interceptor will include the
     * current correlation ID; see {@link BeanLogger#BeanLogger(boolean)} to disable this behaviour.
     */
    public BeanLogger() {
        this(true);
    }

    /**
     * Creates a new instance of the logger method interceptor.
     *
     * @param includeCorrelationIdInLogs a flag to determine whether the correlation ID should be explicitly included
     *                                   in the log statements added by the interceptor.
     */
    public BeanLogger(final boolean includeCorrelationIdInLogs) {
        super(includeCorrelationIdInLogs);
    }

    /**
     * Outputs DEBUG level logging when a public method intercepted by this interceptor is called and when it
     * returns (without exception). If TRACE level logging is enabled and the method has a non-<code>void</code>
     * return type, the return value will be included in the logging. For example:
     *
     * <pre>
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyBean.read(4)
     * ...
     * [40872057-a1b6-4fdd-bce1-7882929bbce6] MyBean.read Returned: MyEntity(id=4, name=test)
     * </pre>
     *
     * @param invocation the method invocation required for implementing a {@link MethodInterceptor}.
     *
     * @return the value returned from the method logged.
     *
     * @throws Throwable thrown if the method logged throws a {@link Throwable}.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return super.log(new MethodInvocationAdapter(invocation));
    }
}
