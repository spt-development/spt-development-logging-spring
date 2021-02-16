package com.spt.development.logging.spring;

import com.spt.development.logging.NoLogging;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;


import static com.spt.development.logging.spring.LoggerUtil.MASKED_ARG;
import static com.spt.development.logging.spring.LoggerUtil.MAX_DEBUG_STR_ARG_LEN;
import static com.spt.development.logging.spring.LoggerUtil.formatArgs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class LoggerUtilTest {
    private interface TestData {
        String TEST_CLASS = "Test Class";
        String SHORT_STRING = "Short String";
        String MEDIUM_STRING = "String with 75 characters--------------------------------------------------";
        String LONG_STRING =   "Very long String with one more character than the maximum (of 75 characters)";
    }

    @Test
    void formatArgs_standardArgs_shouldJoinArgsWithCommas() {
        final String result = formatArgs(
                new Annotation[][] { { new ConcreteAnnotation() }, {}, {} },
                new Object[] { new TestClass(), TestData.SHORT_STRING, TestData.MEDIUM_STRING }
        );

        assertThat(result, is(TestData.TEST_CLASS + ", '" + TestData.SHORT_STRING + "', '" + TestData.MEDIUM_STRING + "'"));
    }

    @Test
    public void formatArgs_stringArgExceedingMaxLength_shouldTruncateLongStringArgs() {
        final String result = formatArgs(
                new Annotation[][] { {}, {} },
                new Object[] { TestData.LONG_STRING, TestData.MEDIUM_STRING }
        );

        assertThat(result, is("'" + TestData.LONG_STRING.substring(0, MAX_DEBUG_STR_ARG_LEN - 3) + "...', '" + TestData.MEDIUM_STRING + "'"));
    }

    @Test
    public void formatArgs_argAnnotatedWithNoLogging_shouldNotBeLogged() {
        final String result = formatArgs(
                new Annotation[][] { { new ConcreteNoLogging() }, { new ConcreteNoLogging() } },
                new Object[] { TestData.LONG_STRING, new TestClass() }
        );

        assertThat(result, is(MASKED_ARG + ", " + MASKED_ARG));
    }

    private static class TestClass {
        @Override
        public String toString() {
            return TestData.TEST_CLASS;
        }
    }

    private static class ConcreteNoLogging implements NoLogging {

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    }

    private static class ConcreteAnnotation implements Annotation {

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    }
}
