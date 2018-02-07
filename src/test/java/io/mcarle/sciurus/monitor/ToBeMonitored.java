package io.mcarle.sciurus.monitor;

import io.mcarle.sciurus.annotation.LoggingMonitorParams;
import io.mcarle.sciurus.annotation.Monitor;

import java.time.temporal.ChronoUnit;

public class ToBeMonitored {

    private final static long TIME = 150;

    @Monitor
    public void doSomething() throws InterruptedException {
        Thread.sleep(TIME);
    }

    @Monitor
    @LoggingMonitorParams(logResult = true)
    public double calcSomething() throws InterruptedException {
        Thread.sleep(TIME);
        return Math.random();
    }

    @Monitor
    @LoggingMonitorParams(logResult = true, logParameter = true)
    public double calcSomethingWith(double number) throws InterruptedException {
        Thread.sleep(TIME);
        return number;
    }

    @Monitor
    @LoggingMonitorParams(logParameter = true)
    public boolean doSomethingWith(boolean something) throws InterruptedException {
        Thread.sleep(TIME);
        return something;
    }

    @Monitor
    @LoggingMonitorParams(warnLimit = 10)
    public void warnIfTooLong(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    @Monitor
    public void callOtherMonitoredMethod() throws InterruptedException {
        doSomething();
    }

    @Monitor
    @LoggingMonitorParams(warnLimit = 11, warnLimitUnit = ChronoUnit.SECONDS)
    public void waitFor(long ms, int ns) throws InterruptedException {
        Thread.sleep(ms, ns);
    }

    @Monitor
    public void nothing() {
    }

    @Monitor
    @LoggingMonitorParams(logResult = true)
    public void throwException() throws InterruptedException {
        Thread.sleep(TIME);
        throw new RuntimeException("Some failure");
    }
}
