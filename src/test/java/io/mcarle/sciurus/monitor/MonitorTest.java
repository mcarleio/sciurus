package io.mcarle.sciurus.monitor;

import io.mcarle.sciurus.Sciurus;
import io.mcarle.sciurus.log4j.TestLevelAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MonitorTest {

    private TestLevelAppender appender;
    private final static String NAME = ToBeMonitored.class.getName();

    @Before
    public void initAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        appender = (TestLevelAppender) config.getAppenders().get(TestLevelAppender.NAME);
        appender.clear();

        Sciurus.startMonitor();
    }

    @Test
    public void forBetterCodeCoverage() {
        new MonitorAspectHandler();
        new LoggingMonitor.LoggingMonitorParamsAnnotationClass();
    }

    @Test
    public void testMonitor() throws InterruptedException {
        new ToBeMonitored().doSomething();

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#doSomething$"));
    }

    @Test
    public void testMonitorNanosOrMicros() {
        new ToBeMonitored().nothing();

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\([ \\d]{4} [nµ]s\\) - " + NAME + "#nothing"));
    }

    @Test
    public void testMonitorMicros() throws InterruptedException {
        new ToBeMonitored().waitFor(0, 1);

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\([ \\d]{4} µs\\) - " + NAME + "#waitFor"));
    }

    @Test
    public void testMonitorMillis() throws InterruptedException {
        new ToBeMonitored().waitFor(12, 0);

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\([ \\d]{4} ms\\) - " + NAME + "#waitFor"));
    }

    @Test
    public void testMonitorSeconds() throws InterruptedException {
        new ToBeMonitored().waitFor(10610, 0);

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\(  11 s \\) - " + NAME + "#waitFor"));
    }

    @Test
    public void testMonitorWithResult() throws InterruptedException {
        double result = new ToBeMonitored().calcSomething();

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#calcSomething : " + result + "$"));
    }

    @Test
    public void testMonitorWithParams() throws InterruptedException {
        boolean something = new ToBeMonitored().doSomethingWith(true);

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#doSomethingWith\\(\\[" + something + "\\]\\)$"));
    }

    @Test
    public void testMonitorWithParamsAndResult() throws InterruptedException {
        double result = new ToBeMonitored().calcSomethingWith(1.0);

        assertEquals(1, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#calcSomethingWith\\(\\[" + result + "\\]\\) : " + result + "$"));
    }

    @Test
    public void testMonitorWarning() throws InterruptedException {
        new ToBeMonitored().warnIfTooLong(20);

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\(  \\d{2} ms\\) - " + NAME + "#warnIfTooLong$"));
        assertEquals(Level.WARN, appender.getLogs().get(0).getKey());
    }

    @Test
    public void testMonitorNoWarning() throws InterruptedException {
        new ToBeMonitored().warnIfTooLong(5);

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\(\\d{4} µs\\) - " + NAME + "#warnIfTooLong$"));
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
    }

    @Test
    public void testMonitorInsideMonitor() throws InterruptedException {
        new ToBeMonitored().callOtherMonitoredMethod();

        assertEquals(2, appender.getMessages().size());
        assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
        assertEquals(Level.DEBUG, appender.getLogs().get(1).getKey());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#doSomething$"));
        assertThat(appender.getMessages().get(1), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#callOtherMonitoredMethod$"));
    }

    @Test
    public void testMonitorThrowingException() throws InterruptedException {
        try {
            new ToBeMonitored().throwException();
            fail();
        } catch (RuntimeException re) {
            assertEquals(1, appender.getMessages().size());
            assertEquals(Level.DEBUG, appender.getLogs().get(0).getKey());
            assertThat(appender.getMessages().get(0), matchesPattern("^\\( 1\\d{2} ms\\) - " + NAME + "#throwException : " + re.toString() + "$"));
        }
    }

    @Test
    public void testStopMonitor() throws InterruptedException {
        Sciurus.stopMonitor();
        new ToBeMonitored().doSomething();
        assertEquals(0, appender.getMessages().size());

        Sciurus.startMonitor();
        new ToBeMonitored().doSomething();
        assertEquals(1, appender.getMessages().size());
    }

    @Test
    public void monitorRegisterTest() throws InterruptedException {
        TestMonitor monitor = new TestMonitor();
        Sciurus.registerMonitor(monitor);
        Sciurus.deregisterMonitor(LoggingMonitor.INSTANCE);
        try {
            new ToBeMonitored().warnIfTooLong(100);
            new ToBeMonitored().warnIfTooLong(200);
            new ToBeMonitored().warnIfTooLong(300);
            new ToBeMonitored().warnIfTooLong(400);
            assertEquals(0, appender.getMessages().size());

            Optional<Method> method = monitor.getMethodExecutionDurations().keySet().stream().findFirst();
            assertTrue(method.isPresent());

            assertThat(
                    (double) getDurationInMillis(monitor.getAvgExecutionDuration(method.get())),
                    closeTo(250, 10)
            );

        } finally {
            Sciurus.registerMonitor(LoggingMonitor.INSTANCE);
        }
    }

    @Test
    public void useClassAnnotationTest() throws InterruptedException {
        new AnnotatedToBeMonitored().warnIfTooLong(50);

        assertEquals(1, appender.getMessages().size());
        assertThat(appender.getMessages().get(0), matchesPattern("^\\(  \\d{2} ms\\) - " + AnnotatedToBeMonitored.class.getName() + "#warnIfTooLong"));
        assertEquals(Level.WARN, appender.getLogs().get(0).getKey());
    }

    private static long getDurationInMillis(Duration duration) {
        return (duration.toNanos() + 500_000) / 1_000_000;
    }

    class TestMonitor implements CustomMonitor {

        private Map<Method, Collection<Duration>> methodExecutionDurations = new HashMap<>();

        @Override
        public void monitored(Duration duration, String declaringTypeName, Method method, Object[] methodArgs, Throwable throwable, Class returnType, Object returnValue) {
            Collection<Duration> durations = methodExecutionDurations.getOrDefault(method, new ArrayList<>());
            durations.add(duration);
            methodExecutionDurations.putIfAbsent(method, durations);
        }

        Map<Method, Collection<Duration>> getMethodExecutionDurations() {
            return Collections.unmodifiableMap(methodExecutionDurations);
        }

        Duration getAvgExecutionDuration(Method method) {
            Collection<Duration> durations = Collections.unmodifiableCollection(methodExecutionDurations.getOrDefault(method, new ArrayList<>()));
            long resultNanos = 0;
            int elements = durations.size();
            for (Duration duration : durations) {
                resultNanos += (duration.getSeconds() * 1_000_000_000 + duration.getNano()) / elements;
            }
            return Duration.of(resultNanos, ChronoUnit.NANOS);
        }
    }

}
