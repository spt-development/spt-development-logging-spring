package com.spt.development.logging.spring.invocation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public class ProceedingJoinPointAdapter implements LoggedInvocation {
    private final ProceedingJoinPoint point;

    public ProceedingJoinPointAdapter(ProceedingJoinPoint proceedingJoinPoint) {
        this.point = proceedingJoinPoint;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return point.getSignature().getDeclaringType();
    }

    @Override
    public Method getMethod() {
        return ((MethodSignature) point.getSignature()).getMethod();
    }

    @Override
    public Object[] getArgs() {
        return point.getArgs();
    }

    @Override
    public Object proceed() throws Throwable {
        return point.proceed();
    }
}
