package com.spt.development.logging.spring.annotation;

import ch.qos.logback.classic.Level;
import com.spt.development.logging.NoLogging;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static com.spt.development.test.LogbackUtil.verifyLogging;
import static com.spt.development.test.LogbackUtil.verifyNoLogging;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { BeanLoggerConfigurationTest.TestBeanConfiguration.class })
class BeanLoggerConfigurationTest {
    private static final class TestData {
        static final String CORRELATION_ID = "19a1060b-e2b2-4acc-af6c-7beea9ac87f2";
        static final String RESULT = "Success!";
        static final String ARG1 = "TestArg";
        static final String ARG2 = "TestArg2";
    }

    @Autowired private TestTarget testTarget;

    @Test
    void callMethod_onBeanLoggerEnabledBean_shouldLogStartAndEndOfMethod() {
        verifyLogging(
            TestTarget.class,
            () -> testTarget.test(TestData.ARG1, TestData.ARG2),
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
    void callMethod_onBeanLoggerExcludedBean_shouldNotLog() {
        verifyNoLogging(
            ExcludedTestTarget.class,
            () -> testTarget.test(TestData.ARG1, TestData.ARG2)
        );
    }

    @Test
    void callMethod_onNoLoggingBean_shouldNotLog() {
        verifyNoLogging(
            NotLoggedTestTarget.class,
            () -> testTarget.test(TestData.ARG1, TestData.ARG2)
        );
    }

    @Test
    void enableBean_withNoIncludeBasePackageClasses_shouldThrowException() {
        final AnnotationMetadata importMetadata = Mockito.mock(AnnotationMetadata.class);

        when(importMetadata.getAnnotationAttributes(anyString(), anyBoolean()))
            .thenReturn(Collections.singletonMap("includeBasePackageClasses", new Class<?>[0]));

        final BeanLoggerConfiguration target = new BeanLoggerConfiguration();

        final IllegalStateException result = assertThrows(IllegalStateException.class, () -> target.setImportMetadata(importMetadata));

        assertThat(
            result.getMessage(),
            is(
                "Property 'includeBasePackageClasses' must contain at least one class, when enabling bean logging "
                    + "with @EnableBeanLogger annotation"
            )
        );
    }

    @Configuration
    @EnableBeanLogging(
        excludedClasses = ExcludedTestTarget.class,
        includeBasePackageClasses = {
            TestTarget.class, NotLoggedTestTarget.class
        }
    )
    static class TestBeanConfiguration {
        @Bean
        TestTarget beanLoggerConfigurationTestTarget() {
            return new TestTarget();
        }
    }

    public static class TestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }
    }

    public static class ExcludedTestTarget {
        public String test(String correlationId, @NoLogging String password) {
            return TestData.RESULT;
        }
    }

    @NoLogging
    public static class NotLoggedTestTarget {
        public String test(String correlationId, String password) {
            return TestData.RESULT;
        }
    }
}