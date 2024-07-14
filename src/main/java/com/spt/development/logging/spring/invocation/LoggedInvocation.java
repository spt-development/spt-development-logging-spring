package com.spt.development.logging.spring.invocation;

import java.lang.reflect.Method;

public interface LoggedInvocation {
    Class<?> getDeclaringClass();

    Method getMethod();

    Object[] getArgs();

    Object proceed() throws Throwable;
}
