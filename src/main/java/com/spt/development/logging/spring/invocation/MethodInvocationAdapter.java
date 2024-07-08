package com.spt.development.logging.spring.invocation;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class MethodInvocationAdapter implements LoggedInvocation {
    private final MethodInvocation methodInvocation;

    public MethodInvocationAdapter(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return methodInvocation.getMethod().getDeclaringClass();
    }

    @Override
    public Method getMethod() {
        return methodInvocation.getMethod();
    }

    @Override
    public Object[] getArgs() {
        return methodInvocation.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return methodInvocation.proceed();
    }
}
