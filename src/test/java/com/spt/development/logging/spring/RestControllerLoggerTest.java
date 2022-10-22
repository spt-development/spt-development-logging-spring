package com.spt.development.logging.spring;

import ch.qos.logback.classic.Level;
import com.spt.development.cid.CorrelationId;
import com.spt.development.logging.NoLogging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static com.spt.development.test.LogbackUtil.verifyLogging;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class RestControllerLoggerTest {
    private interface TestData {
        String CORRELATION_ID = "52d676d9-81f3-4167-a078-09a1c2ed9a01";
        String RESULT = "Success!";
        String METHOD = "test";
        String ARG1 = "TestArg";
        String ARG2 = "TestArg2";
    }

    @BeforeEach
    void setUp() {
        CorrelationId.set(TestData.CORRELATION_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_joinPointWithReturnValue_shouldReturnJoinPointResult(boolean includeCorrelationIdInLogs) throws Throwable {
        final Object result = createLogger(includeCorrelationIdInLogs).log(createJoinPoint(String.class, TestData.RESULT));

        assertThat(result, is(TestData.RESULT));
    }

    @Test
    void log_joinPointWithReturnValue_shouldLogStartAndEndOfMethodWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint(String.class, TestData.RESULT));
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.TRACE));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test Returned: " + TestData.RESULT));
                }
        );
    }

    @Test
    void log_joinPointWithReturnValue_shouldLogStartAndEndOfMethodWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(createJoinPoint(String.class, TestData.RESULT));
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.TRACE));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test Returned: " + TestData.RESULT));
                }
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_joinPointWithVoidReturnType_shouldReturnNull(boolean includeCorrelationIdInLogs) throws Throwable {
        final Object result = createLogger(includeCorrelationIdInLogs).log(createJoinPoint(void.class));

        assertThat(result, is(nullValue()));
    }

    @Test
    void log_joinPointWithVoidReturnValue_shouldLogStartAndEndOfMethodWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint(void.class));
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test - complete"));
                }
        );
    }

    @Test
    void log_joinPointWithVoidReturnValue_shouldLogStartAndEndOfMethodWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(createJoinPoint(void.class));
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test - complete"));
                }
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_proceedThrowsUnexpectedException_shouldThrowException(boolean includeCorrelationIdInLogs) throws Throwable {
        final ProceedingJoinPoint joinPoint = createJoinPoint(new Exception("test"));
        final RestControllerLogger target = createLogger(includeCorrelationIdInLogs);

        assertThrows(Exception.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsUnexpectedException_shouldLogExceptionAsErrorWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint(new Exception("test")));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.ERROR));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception:"));
                    assertThat(logs.get(1).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(1).getThrowableProxy().getClassName(), is("java.lang.Exception"));
                    assertThat(logs.get(1).getThrowableProxy().getMessage(), is("test"));
                }
        );
    }

    @Test
    void log_proceedThrowsUnexpectedException_shouldLogExceptionAsErrorWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(createJoinPoint(new Exception("test")));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.ERROR));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception:"));
                    assertThat(logs.get(1).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(1).getThrowableProxy().getClassName(), is("java.lang.Exception"));
                    assertThat(logs.get(1).getThrowableProxy().getMessage(), is("test"));
                }
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_proceedThrowsHttpServerErrorException_shouldThrowException(boolean includeCorrelationIdInLogs) throws Throwable {
        final ProceedingJoinPoint joinPoint = createJoinPoint(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        final RestControllerLogger target = createLogger(includeCorrelationIdInLogs);

        assertThrows(HttpServerErrorException.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsHttpServerErrorException_shouldLogExceptionAsErrorWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.ERROR));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception:"));
                    assertThat(logs.get(1).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(1).getThrowableProxy().getClassName(), is("org.springframework.web.client.HttpServerErrorException"));
                    assertThat(logs.get(1).getThrowableProxy().getMessage(), is("500 INTERNAL_SERVER_ERROR"));
                }
        );
    }

    @Test
    void log_proceedThrowsHttpServerErrorException_shouldLogExceptionAsErrorWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(createJoinPoint(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.ERROR));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception:"));
                    assertThat(logs.get(1).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(1).getThrowableProxy().getClassName(), is("org.springframework.web.client.HttpServerErrorException"));
                    assertThat(logs.get(1).getThrowableProxy().getMessage(), is("500 INTERNAL_SERVER_ERROR"));
                }
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_proceedThrowsExceptionAnnotatedAsClientException_shouldThrowException(boolean includeCorrelationIdInLogs) throws Throwable {
        final ProceedingJoinPoint joinPoint = createJoinPoint(new DuplicateUserException("Test", new Exception()));
        final RestControllerLogger target = createLogger(includeCorrelationIdInLogs);

        assertThrows(DuplicateUserException.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsExceptionAnnotatedAsClientException_shouldLogExceptionAsInfoWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint(new DuplicateUserException("Test", new Exception())));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(3));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception: com.spt.development.logging.spring.RestControllerLoggerTest.DuplicateUserException"));

                    assertThat(logs.get(2).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(2).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(2).getFormattedMessage(), containsString("Exception:"));
                    assertThat(logs.get(2).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(2).getThrowableProxy().getClassName(), is("com.spt.development.logging.spring.RestControllerLoggerTest$DuplicateUserException"));
                    assertThat(logs.get(2).getThrowableProxy().getMessage(), is("Test"));
                }
        );
    }

    @Test
    void log_proceedThrowsExceptionAnnotatedAsClientException_shouldLogExceptionAsDebugWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(createJoinPoint(new DuplicateUserException("Test", new Exception())));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(3));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception: com.spt.development.logging.spring.RestControllerLoggerTest.DuplicateUserException"));

                    assertThat(logs.get(2).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(2).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(2).getFormattedMessage(), containsString("Exception:"));
                    assertThat(logs.get(2).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(2).getThrowableProxy().getClassName(), is("com.spt.development.logging.spring.RestControllerLoggerTest$DuplicateUserException"));
                    assertThat(logs.get(2).getThrowableProxy().getMessage(), is("Test"));
                }
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_proceedThrowsHttpClientErrorException_shouldThrowException(boolean includeCorrelationIdInLogs) throws Throwable {
        final ProceedingJoinPoint joinPoint = createJoinPoint(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        final RestControllerLogger target = createLogger(includeCorrelationIdInLogs);

        assertThrows(HttpClientErrorException.class, () -> target.log(joinPoint));
    }

    @Test
    void log_proceedThrowsHttpClientErrorException_shouldLogExceptionAsInfoWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint(new HttpClientErrorException(HttpStatus.BAD_REQUEST)));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(3));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception: org.springframework.web.client.HttpClientErrorException"));

                    assertThat(logs.get(2).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(2).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(2).getFormattedMessage(), containsString("Exception:"));
                    assertThat(logs.get(2).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(2).getThrowableProxy().getClassName(), is("org.springframework.web.client.HttpClientErrorException"));
                    assertThat(logs.get(2).getThrowableProxy().getMessage(), is("400 BAD_REQUEST"));
                }
        );
    }

    @Test
    void log_proceedThrowsHttpClientErrorException_shouldLogExceptionAsDebugWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(createJoinPoint(new HttpClientErrorException(HttpStatus.BAD_REQUEST)));
                    } catch (Throwable t) {
                        // Expected exception
                        return null;
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(3));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test threw exception: org.springframework.web.client.HttpClientErrorException"));

                    assertThat(logs.get(2).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(2).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(2).getFormattedMessage(), containsString("Exception:"));
                    assertThat(logs.get(2).getThrowableProxy(), is(notNullValue()));
                    assertThat(logs.get(2).getThrowableProxy().getClassName(), is("org.springframework.web.client.HttpClientErrorException"));
                    assertThat(logs.get(2).getThrowableProxy().getMessage(), is("400 BAD_REQUEST"));
                }
        );
    }

    private ProceedingJoinPoint createJoinPoint(Class<Void> methodReturnType) throws Throwable {
        return createJoinPoint(methodReturnType, (Void)null);
    }

    private <T> ProceedingJoinPoint createJoinPoint(Class<T> methodReturnType, T returnValue) throws Throwable {
        return createJoinPoint(methodReturnType, returnValue, null);
    }

    private <T> ProceedingJoinPoint createJoinPoint(Exception exception) throws Throwable {
        return createJoinPoint(void.class, exception);
    }

    private <T> ProceedingJoinPoint createJoinPoint(Class<T> methodReturnType, Exception exception) throws Throwable {
        return createJoinPoint(methodReturnType, null, exception);
    }

    private <T> ProceedingJoinPoint createJoinPoint(Class<T> methodReturnType, T returnValue, Exception exception) throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);

        if (exception != null) {
            when(joinPoint.proceed()).thenThrow(exception);
        } else {
            when(joinPoint.proceed()).thenReturn(returnValue);
        }
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(methodReturnType);

        return joinPoint;
    }

    private static RestControllerLogger createLogger(boolean includeCorrelationIdInLogs) {
        return includeCorrelationIdInLogs ? new RestControllerLogger() : new RestControllerLogger(false);
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