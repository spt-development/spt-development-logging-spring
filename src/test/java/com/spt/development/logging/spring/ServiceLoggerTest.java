package com.spt.development.logging.spring;

import com.spt.development.logging.NoLogging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

class ServiceLoggerTest {
    private interface TestData {
        String RESULT = "Success!";
        String METHOD = "test";
        String ARG1 = "TestArg";
        String ARG2 = "TestArg2";
    }

    @Test
    void log_joinPointWithReturnValue_shouldReturnJoinPointResult() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.proceed()).thenReturn(TestData.RESULT);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(String.class);

        final Object result = createLogger().log(joinPoint);

        assertThat(result, is(TestData.RESULT));
    }

    @Test
    void log_joinPointWithVoidReturnType_shouldReturnNull() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(void.class);

        final Object result = createLogger().log(joinPoint);

        assertThat(result, is(nullValue()));
    }

    private ServiceLogger createLogger() {
        return new ServiceLogger();
    }

    private static class TestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }
    }
}