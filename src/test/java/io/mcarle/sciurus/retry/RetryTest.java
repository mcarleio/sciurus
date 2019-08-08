package io.mcarle.sciurus.retry;

import io.mcarle.sciurus.log4j.TestLevelAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.*;

public class RetryTest {

    private TestLevelAppender appender;

    @Before
    public void cleanAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        appender = (TestLevelAppender) config.getAppenders().get(TestLevelAppender.NAME);

        appender.clear();
    }

    @Test
    public void forBetterCodeCoverage() {
        new RetryAspectHandler();
    }


    @Test
    public void successfullMethodWithOneRetry() {
        new MethodsAnnotatedWithRetry().successfullMethodWithOneRetry();

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successfullMethodWithOneRetry"));
    }

    @Test
    public void successfullMethodWithZeroRetry() {
        new MethodsAnnotatedWithRetry().successfullMethodWithZeroRetry();

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successfullMethodWithZeroRetry"));
    }

    @Test
    public void successfullMethodWithMinusFiveRetry() {
        new MethodsAnnotatedWithRetry().successfullMethodWithMinusFiveRetry();

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successfullMethodWithMinusFiveRetry"));
    }

    @Test
    public void successfullMethodWithFiveRetry() {
        new MethodsAnnotatedWithRetry().successfullMethodWithFiveRetry();

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successfullMethodWithFiveRetry"));
    }

    @Test
    public void exceptionThrowingWithOneRetry() {
        try {
            new MethodsAnnotatedWithRetry().exceptionThrowingWithOneRetry();
            fail();
        } catch (Throwable t) {
            assertEquals(2, appender.getMessages().size());
            assertThat(appender.getMessages().get(0), matchesPattern("exceptionThrowingWithOneRetry"));
            assertThat(appender.getMessages().get(1), matchesPattern("exceptionThrowingWithOneRetry"));
            assertThat(t, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void exceptionThrowingWithZeroRetries() {
        try {
            new MethodsAnnotatedWithRetry().exceptionThrowingWithZeroRetries();
            fail();
        } catch (Throwable t) {
            assertEquals(1, appender.getMessages().size());
            assertThat(appender.getMessages().get(0), matchesPattern("exceptionThrowingWithZeroRetries"));
            assertThat(t, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void exceptionThrowingWithMinusFiveRetries() {
        try {
            new MethodsAnnotatedWithRetry().exceptionThrowingWithMinusFiveRetries();
            fail();
        } catch (Throwable t) {
            assertEquals(1, appender.getMessages().size());
            assertThat(appender.getMessages().get(0), matchesPattern("exceptionThrowingWithMinusFiveRetries"));
            assertThat(t, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void exceptionThrowingWithFiveRetry() {
        try {
            new MethodsAnnotatedWithRetry().exceptionThrowingWithFiveRetry();
            fail();
        } catch (Throwable t) {
            assertEquals(6, appender.getMessages().size());
            assertThat(appender.getMessages().get(0), matchesPattern("exceptionThrowingWithFiveRetry"));
            assertThat(appender.getMessages().get(1), matchesPattern("exceptionThrowingWithFiveRetry"));
            assertThat(appender.getMessages().get(2), matchesPattern("exceptionThrowingWithFiveRetry"));
            assertThat(appender.getMessages().get(3), matchesPattern("exceptionThrowingWithFiveRetry"));
            assertThat(appender.getMessages().get(4), matchesPattern("exceptionThrowingWithFiveRetry"));
            assertThat(appender.getMessages().get(5), matchesPattern("exceptionThrowingWithFiveRetry"));
            assertThat(t, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void successAfterXTriesWithTwoRetries() {
        new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetries(1);
        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetries"));
        appender.clear();

        new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetries(2);
        assertEquals(2, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetries"));
        assertThat(appender.getMessages().get(1), matchesPattern("successAfterXTriesWithTwoRetries"));
        appender.clear();

        new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetries(3);
        assertEquals(3, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetries"));
        assertThat(appender.getMessages().get(1), matchesPattern("successAfterXTriesWithTwoRetries"));
        assertThat(appender.getMessages().get(2), matchesPattern("successAfterXTriesWithTwoRetries"));
        appender.clear();

        try {
            new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetries(4);
            fail();
        } catch (Throwable t) {
            assertEquals(3, appender.getMessages().size());
            assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetries"));
            assertThat(appender.getMessages().get(1), matchesPattern("successAfterXTriesWithTwoRetries"));
            assertThat(appender.getMessages().get(2), matchesPattern("successAfterXTriesWithTwoRetries"));
            assertThat(t, instanceOf(RuntimeException.class));
        }
    }

    @Test
    public void successAfterXTriesWithTwoRetriesReturnResult() {
        int result;
        result = new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetriesReturnResult(1);
        assertEquals(1, result);
        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
        appender.clear();

        result = new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetriesReturnResult(2);
        assertEquals(2, result);
        assertEquals(2, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
        assertThat(appender.getMessages().get(1), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
        appender.clear();

        result = new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetriesReturnResult(3);
        assertEquals(3, result);
        assertEquals(3, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
        assertThat(appender.getMessages().get(1), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
        assertThat(appender.getMessages().get(2), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
        appender.clear();

        try {
            new MethodsAnnotatedWithRetry().successAfterXTriesWithTwoRetriesReturnResult(4);
            fail();
        } catch (Throwable t) {
            assertEquals(3, appender.getMessages().size());
            assertThat(appender.getMessages().get(0), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
            assertThat(appender.getMessages().get(1), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
            assertThat(appender.getMessages().get(2), matchesPattern("successAfterXTriesWithTwoRetriesReturnResult"));
            assertThat(t, instanceOf(RuntimeException.class));
        }
    }

}
