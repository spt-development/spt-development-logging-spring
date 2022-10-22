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

import static com.spt.development.test.LogbackUtil.verifyLogging;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

class RepositoryLoggerTest {
    private interface TestData {
        String CORRELATION_ID = "2f0c045f-7547-438c-8496-700126b4d1f8";
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

                    assertThat(logs.get(0).getLevel(), is(Level.DEBUG));
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

                    assertThat(logs.get(0).getLevel(), is(Level.DEBUG));
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

                    assertThat(logs.get(0).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.DEBUG));
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

                    assertThat(logs.get(0).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test - complete"));
                }
        );
    }

    private ProceedingJoinPoint createJoinPoint(Class<Void> methodReturnType) throws Throwable {
        return createJoinPoint(methodReturnType, null);
    }

    private <T> ProceedingJoinPoint createJoinPoint(Class<T> methodReturnType, T returnValue) throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.proceed()).thenReturn(returnValue);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));
        when(methodSignature.getReturnType()).thenReturn(methodReturnType);

        return joinPoint;
    }

    private RepositoryLogger createLogger(boolean includeCorrelationIdInLogs) {
        return includeCorrelationIdInLogs ? new RepositoryLogger() : new RepositoryLogger(false);
    }

    private static class TestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }
    }
}