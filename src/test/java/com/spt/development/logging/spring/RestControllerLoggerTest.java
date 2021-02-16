package com.spt.development.logging.spring;

import com.spt.development.logging.NoLogging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class RestControllerLoggerTest {
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

    @Test
    void log_proceedThrowsUnexepectedException_shouldThrowException() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });
        when(joinPoint.proceed()).thenThrow(new Exception("test"));

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(void.class);

        final RestControllerLogger target = createLogger();

        assertThrows(Exception.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsHttpServerErrorException_shouldThrowException() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });
        when(joinPoint.proceed()).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(void.class);

        final RestControllerLogger target = createLogger();

        assertThrows(HttpServerErrorException.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsExceptionAnnotatedAsClientException_shouldThrowException() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });
        when(joinPoint.proceed()).thenThrow(new DuplicateUserException("Test", new Exception()));

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(void.class);

        final RestControllerLogger target = createLogger();

        assertThrows(DuplicateUserException.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsHttpClientErrorException_shouldThrowException() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });
        when(joinPoint.proceed()).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(void.class);

        final RestControllerLogger target = createLogger();

        assertThrows(HttpClientErrorException.class, () -> target.log(joinPoint));
    }

    private RestControllerLogger createLogger() {
        return new RestControllerLogger();
    }

    private static class TestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Email address already in use")
    private static class DuplicateUserException extends Exception {
        static final long serialVersionUID = 1L;

        public DuplicateUserException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}