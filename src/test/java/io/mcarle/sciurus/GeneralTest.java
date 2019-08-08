package io.mcarle.sciurus;

import io.mcarle.sciurus.cache.CacheAspect;
import io.mcarle.sciurus.cache.CacheRegister;
import io.mcarle.sciurus.cache.ExpiringMapCache;
import io.mcarle.sciurus.lock.LockAspect;
import io.mcarle.sciurus.monitor.LoggingMonitor;
import io.mcarle.sciurus.monitor.MonitorAspect;
import io.mcarle.sciurus.monitor.MonitorRegister;
import io.mcarle.sciurus.retry.RetryAspect;
import org.aspectj.lang.Aspects;
import org.junit.Test;

import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class GeneralTest {

    @Test
    public void callAspectMethods() {
        Aspects.aspectOf(MonitorAspect.class);
        Aspects.hasAspect(MonitorAspect.class);
        Aspects.aspectOf(CacheAspect.class);
        Aspects.hasAspect(CacheAspect.class);
        Aspects.aspectOf(LockAspect.class);
        Aspects.hasAspect(LockAspect.class);
        Aspects.aspectOf(RetryAspect.class);
        Aspects.hasAspect(RetryAspect.class);
    }

    @Test
    public void listEnums() {
        Stream.of(CacheRegister.values()).map(CacheRegister::name).forEach(CacheRegister::valueOf);
        Stream.of(ExpiringMapCache.values()).map(ExpiringMapCache::name).forEach(ExpiringMapCache::valueOf);
        Stream.of(MonitorRegister.values()).map(MonitorRegister::name).forEach(MonitorRegister::valueOf);
        Stream.of(LoggingMonitor.values()).map(LoggingMonitor::name).forEach(LoggingMonitor::valueOf);
    }

    @Test
    public void equalsOfExecutionIdentifier() {
        ExecutionIdentifier ei = new ExecutionIdentifier("abc", new Object[0]);
        assertFalse(ei.equals(null));
        assertFalse(ei.equals(new Object()));
        assertFalse(ei.equals(new ExecutionIdentifier("def", new Object[0])));
        assertFalse(ei.equals(new ExecutionIdentifier("abc", new Object[]{"ABC"})));

        assertTrue(ei.equals(new ExecutionIdentifier("abc", new Object[0])));
    }

    @Test
    public void initializeStaticClasses() {
        new Sciurus();
    }
}
