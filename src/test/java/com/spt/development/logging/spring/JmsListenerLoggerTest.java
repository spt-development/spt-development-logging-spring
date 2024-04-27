package com.spt.development.logging.spring;

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
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

class JmsListenerLoggerTest {
    private static final class TestData {
        static final String CORRELATION_ID = "f3867dd5-b137-4c05-8816-69fd262024b7";
        static final String RESULT = "Success!";
        static final String METHOD = "test";
        static final String ARG1 = "TestArg";
        static final String ARG2 = "TestArg2";
    }

    @BeforeEach
    void setUp() {
        CorrelationId.set(TestData.CORRELATION_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void log_validJoinPoint_shouldReturnJoinPointResult(boolean includeCorrelationIdInLogs) throws Throwable {
        final Object result = createLogger(includeCorrelationIdInLogs).log(createJoinPoint());

        assertThat(result, is(TestData.RESULT));
    }

    @Test
    void log_validJoinPoint_shouldLogStartAndEndOfMethodWithoutCorrelationId() {
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

                    assertThat(logs.get(0).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getFormattedMessage(), not(startsWith("[" + TestData.CORRELATION_ID + "]")));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test - complete"));
                }
        );
    }

    @Test
    void log_validJoinPoint_shouldLogStartAndEndOfMethodWithCorrelationId() {
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

                    assertThat(logs.get(0).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(0).getFormattedMessage(), containsString("TestTarget.test('TestArg', ******"));

                    assertThat(logs.get(1).getFormattedMessage(), startsWith("[" + TestData.CORRELATION_ID + "]"));
                    assertThat(logs.get(1).getFormattedMessage(), containsString("TestTarget.test - complete"));
                }
        );
    }

    private ProceedingJoinPoint createJoinPoint() throws Throwable {
        final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.proceed()).thenReturn(TestData.RESULT);
        when(joinPoint.getTarget()).thenReturn(new TestTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[] { TestData.ARG1, TestData.ARG2 });

        when(methodSignature.getDeclaringType()).thenReturn(TestTarget.class);
        when(methodSignature.getName()).thenReturn(TestData.METHOD);
        when(methodSignature.getMethod()).thenReturn(TestTarget.class.getMethod(TestData.METHOD, String.class, String.class));

        return joinPoint;
    }

    private JmsListenerLogger createLogger(boolean includeCorrelationIdInLogs) {
        return includeCorrelationIdInLogs ? new JmsListenerLogger() : new JmsListenerLogger(false);
    }

    private static final class TestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }
    }
}