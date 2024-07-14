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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.spt.development.test.LogbackUtil.verifyLogging;
import static com.spt.development.test.LogbackUtil.verifyNoLogging;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

class DaoSupportLoggerTest {
    private static final class TestData {
        static final String CORRELATION_ID = "2f0c045f-7547-438c-8496-700126b4d1f8";
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
                        return createLogger(true).log(createJoinPoint());
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
        final Object result = createLogger(includeCorrelationIdInLogs).log(
            createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null)
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
                            createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null)
                        );
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.testVoid('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.DEBUG));
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
                            createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null)
                        );
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                },
                (logs) -> {
                    assertThat(logs, is(notNullValue()));
                    assertThat(logs.size(), is(2));

                    assertThat(logs.get(0).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.testVoid('TestArg', ******"));

                    assertThat(logs.get(1).getLevel(), is(Level.DEBUG));
                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.testVoid - complete"));
                }
        );
    }

    @Test
    void log_logLevelAtInfo_shouldNotLogStartAndEndOfMethod() {
        verifyNoLogging(
            TestTargetLoggedAtInfo.class,
            () -> {
                try {
                    return createLogger(true).log(
                        createJoinPoint(TestTargetLoggedAtInfo.class, TestTargetLoggedAtInfo.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null)
                    );
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_joinPointWithReturnValueAndLogLevelAtInfo_shouldReturnJoinPointResult(boolean includeCorrelationIdInLogs) throws Throwable {
        final Object result = createLogger(includeCorrelationIdInLogs).log(
            createJoinPoint(TestTargetLoggedAtInfo.class, TestTargetLoggedAtInfo.class.getMethod(TestData.METHOD_STR_RETURN, String.class, String.class), TestData.RESULT)
        );
        assertThat(result, is(TestData.RESULT));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_joinPointWithVoidReturnTypeAndLogLevelAtInfo_shouldReturnNull(boolean includeCorrelationIdInLogs) throws Throwable {
        final Object result = createLogger(includeCorrelationIdInLogs).log(
            createJoinPoint(TestTargetLoggedAtInfo.class, TestTargetLoggedAtInfo.class.getMethod(TestData.METHOD_VOID_RETURN, String.class, String.class), null)
        );
        assertThat(result, is(nullValue()));
    }

    private ProceedingJoinPoint createJoinPoint() throws Throwable {
        return createJoinPoint(TestTarget.class, TestTarget.class.getMethod(TestData.METHOD_STR_RETURN, String.class, String.class), TestData.RESULT);
    }

    private <T, U> ProceedingJoinPoint createJoinPoint(Class<T> target, Method method, U returnValue) throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.proceed()).thenReturn(returnValue);

        final Constructor<T> targetConstructor = target.getDeclaredConstructor();

        when(joinPoint.getTarget()).thenReturn(targetConstructor.newInstance());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(target);
        when(methodSignature.getName()).thenReturn(method.getName());
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getReturnType()).thenReturn(method.getReturnType());

        return joinPoint;
    }

    private DaoSupportLogger createLogger(boolean includeCorrelationIdInLogs) {
        return includeCorrelationIdInLogs ? new DaoSupportLogger() : new DaoSupportLogger(false);
    }

    private static final class TestTarget extends DaoSupportLogger {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }

        public void testVoid(String correlationId, @NoLogging String password) {
        }
    }

    private static final class TestTargetLoggedAtInfo extends DaoSupportLogger {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }

        public void testVoid(String correlationId, @NoLogging String password) {
        }
    }
}