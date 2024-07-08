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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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
    private static final class TestData {
        static final String CORRELATION_ID = "52d676d9-81f3-4167-a078-09a1c2ed9a01";
        static final String RESULT = "Success!";
        static final String METHOD_STR_RETURN = "test";
        static final String METHOD_VOID_RETURN = "testVoid";
        static final String ARG1 = "TestArg";
        static final String ARG2 = "TestArg2";
    }

    @BeforeEach
    void setUp() {
        CorrelationId.set(TestData.CORRELATION_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_joinPointWithReturnValue_shouldReturnJoinPointResult(boolean includeCorrelationIdInLogs) throws Throwable {
        final Object result = createLogger(includeCorrelationIdInLogs).log(createJoinPoint());

        assertThat(result, is(TestData.RESULT));
    }

    @Test
    void log_joinPointWithReturnValue_shouldLogStartAndEndOfMethodWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(createJoinPoint());
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
                        return createLogger(true).log(createJoinPoint());
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
        final Object result = createLogger(includeCorrelationIdInLogs).log(
            createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null, null)
        );
        assertThat(result, is(nullValue()));
    }

    @Test
    void log_joinPointWithVoidReturnValue_shouldLogStartAndEndOfMethodWithoutCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(false).log(
                            createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null, null)
                        );
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.testVoid('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.testVoid - complete"));
                }
        );
    }

    @Test
    void log_joinPointWithVoidReturnValue_shouldLogStartAndEndOfMethodWithCorrelationId() {
        verifyLogging(
                TestTarget.class,
                () -> {
                    try {
                        return createLogger(true).log(
                            createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null, null)
                        );
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.INFO));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.testVoid('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.INFO));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.testVoid - complete"));
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

    private ProceedingJoinPoint createJoinPoint() throws Throwable {
        return createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_STR_RETURN, String.class, String.class), TestData.RESULT, null);
    }

    private ProceedingJoinPoint createJoinPoint(Exception exception) throws Throwable {
        return createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_STR_RETURN, String.class, String.class), TestData.RESULT, exception);
    }

    private <T, U> ProceedingJoinPoint createJoinPoint(Class<T> target, Method method, U returnValue, Exception exception) throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);

        if (exception != null) {
            when(joinPoint.proceed()).thenThrow(exception);
        } else {
            when(joinPoint.proceed()).thenReturn(returnValue);
        }

        final Constructor<T> targetConstructor = target.getDeclaredConstructor();

        when(joinPoint.getTarget()).thenReturn(targetConstructor.newInstance());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(target);
        when(methodSignature.getName()).thenReturn(method.getName());
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getReturnType()).thenReturn(method.getReturnType());

        return joinPoint;
    }

    private static RestControllerLogger createLogger(boolean includeCorrelationIdInLogs) {
        return includeCorrelationIdInLogs ? new RestControllerLogger() : new RestControllerLogger(false);
    }

    private static final class TestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }

        public void testVoid(String correlationId, @NoLogging String password) {
        }
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Email address already in use")
    private static final class DuplicateUserException extends Exception {
        static final long serialVersionUID = 1L;

        DuplicateUserException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}