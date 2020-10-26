package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.ExecutionIdentifier;
import io.mcarle.sciurus.Sciurus;
import io.mcarle.sciurus.log4j.TestLevelAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CacheTest {

    final static long CACHETIME = 100;
    final static String CUSTOM_CACHE_NAME = "CUSTOM";
    private static TestLevelAppender appender;

    @Before
    public void startCache() {
        Sciurus.startCache();
    }

    @After
    public void stopCache() {
        Sciurus.stopCache();
    }

    @Before
    public void initAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        appender = (TestLevelAppender) config.getAppenders().get(TestLevelAppender.NAME);
        appender.clear();
    }

    @Test
    @SuppressWarnings("InstantiationOfUtilityClass")
    public void forBetterCodeCoverage() {
        new CacheAspectHandler();
    }

    @Test
    public void notCacheableBecauseOfVoidTest() throws InterruptedException {
        {
            new ToBeCached().notCacheableBecauseOfVoid();

            long pre = System.currentTimeMillis();
            new ToBeCached().notCacheableBecauseOfVoid();
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
        {
            Object o = new Object();
            new ToBeCached().notCacheableBecauseOfVoid(o);

            long pre = System.currentTimeMillis();
            new ToBeCached().notCacheableBecauseOfVoid(o);
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
        {
            String s = "123456";
            new ToBeCached().notCacheableBecauseOfVoid(s);

            long pre = System.currentTimeMillis();
            new ToBeCached().notCacheableBecauseOfVoid(s);
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
    }

    @Test
    public void notCacheableBecauseOfReturnTypeNotSerializableTest() throws InterruptedException {
        {
            assertNotNull(new ToBeCached().notCacheableBecauseOfReturnTypeNotSerializable());

            long pre = System.currentTimeMillis();
            assertNotNull(new ToBeCached().notCacheableBecauseOfReturnTypeNotSerializable());
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
        {
            Object o = new Object();
            assertNotNull(new ToBeCached().notCacheableBecauseOfReturnTypeNotSerializable(o));

            long pre = System.currentTimeMillis();
            assertNotNull(new ToBeCached().notCacheableBecauseOfReturnTypeNotSerializable(o));
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
        {
            String s = "123456";
            assertNotNull(new ToBeCached().notCacheableBecauseOfReturnTypeNotSerializable(s));

            long pre = System.currentTimeMillis();
            assertNotNull(new ToBeCached().notCacheableBecauseOfReturnTypeNotSerializable(s));
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
    }

    @Test
    public void notCacheableBecauseOfParameterTypeNotSerializableTest() throws InterruptedException {
        {
            Object o = new Object();
            assertEquals(42, new ToBeCached().notCacheableBecauseOfParameterTypeNotSerializable(o));

            long pre = System.currentTimeMillis();
            assertEquals(42, new ToBeCached().notCacheableBecauseOfParameterTypeNotSerializable(o));
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
        {
            Object o = new Object();
            int i = 42;
            assertEquals(42, new ToBeCached().notCacheableBecauseOfParameterTypeNotSerializable(i, o));

            long pre = System.currentTimeMillis();
            assertEquals(42, new ToBeCached().notCacheableBecauseOfParameterTypeNotSerializable(i, o));
            long post = System.currentTimeMillis();

            checkNotCached(post - pre);
        }
    }

    @Test
    public void cacheable() throws InterruptedException {
        {
            int expected = new ToBeCached().cacheable();

            long pre = System.currentTimeMillis();
            int result = new ToBeCached().cacheable();
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkCached(post - pre);
        }
        {
            int i = 42;
            int expected = new ToBeCached().cacheable(i);

            long pre = System.currentTimeMillis();
            int result = new ToBeCached().cacheable(i);
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkCached(post - pre);
        }
        {
            String s = "123456";
            String expected = new ToBeCached().cacheable(s);

            long pre = System.currentTimeMillis();
            String result = new ToBeCached().cacheable(s);
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkCached(post - pre);
        }
        {
            String s = "123456";
            int i = 42;
            long expected = new ToBeCached().cacheable(s, i);

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheable(s, i);
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkCached(post - pre);
        }
    }

    @Test
    public void cacheableWithDifferentParams() throws InterruptedException {
        {
            int expected = new ToBeCached().cacheable(42);

            long pre = System.currentTimeMillis();
            int result = new ToBeCached().cacheable(43);
            long post = System.currentTimeMillis();

            assertNotEquals(expected, result);
            checkNotCached(post - pre);
        }
        {
            String expected = new ToBeCached().cacheable("123456");

            long pre = System.currentTimeMillis();
            String result = new ToBeCached().cacheable("654321");
            long post = System.currentTimeMillis();

            assertNotEquals(expected, result);
            checkNotCached(post - pre);
        }
        {
            long expected = new ToBeCached().cacheable("123456", 42);

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheable("123", 42);
            long post = System.currentTimeMillis();

            assertNotEquals(expected, result);
            checkNotCached(post - pre);
        }
        {
            long expected = new ToBeCached().cacheable("123456", 42);

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheable("123456", 43);
            long post = System.currentTimeMillis();

            assertNotEquals(expected, result);
            checkNotCached(post - pre);
        }
    }

    @Test
    public void expiringMapCacheTest() throws InterruptedException {
        {
            String s = "123456";
            int i = 42;
            long expected = new ToBeCached().cacheable(s, i);

            Thread.sleep(CACHETIME / 2);

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheable(s, i);
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkCached(post - pre);
        }
        {
            String s = "123456";
            int i = 42;
            long expected = new ToBeCached().cacheable(s, i);

            Thread.sleep(CACHETIME);

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheable(s, i);
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkNotCached(post - pre);
        }
    }

    @Test
    public void cacheRegisterTest() throws InterruptedException {
        {
            long expected = new ToBeCached().cacheInCustomCache();

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheInCustomCache();
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkNotCached(post - pre);
            checkUnknownCacheException();
        }
        {
            Sciurus.registerCache(CUSTOM_CACHE_NAME, new TestCache());
            long expected = new ToBeCached().cacheInCustomCache();

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheInCustomCache();
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkCached(post - pre);
            checkNoUnknownCacheException();
        }
        {
            appender.clear();
            assertTrue(Sciurus.deregisterCache(CUSTOM_CACHE_NAME));
            long expected = new ToBeCached().cacheInCustomCache();

            long pre = System.currentTimeMillis();
            long result = new ToBeCached().cacheInCustomCache();
            long post = System.currentTimeMillis();

            assertEquals(expected, result);
            checkNotCached(post - pre);
            checkUnknownCacheException();
        }
    }

    @Test
    public void illegalCacheRegisterTest() throws InterruptedException {
        {
            Sciurus.registerCache(CUSTOM_CACHE_NAME, () -> null);
            new ToBeCached().cacheInCustomCache();
            checkIllegalStateException();
            assertTrue(Sciurus.deregisterCache(CUSTOM_CACHE_NAME));
        }
    }

    @Test
    public void exceptionThrowingCacheSupplierTest() throws InterruptedException {
        {
            Sciurus.registerCache(CUSTOM_CACHE_NAME, () -> {
                throw new RuntimeException("exceptionThrowingCacheSupplierTest");
            });
            new ToBeCached().cacheInCustomCache();
            checkExceptionFromSupplier();
            assertTrue(Sciurus.deregisterCache(CUSTOM_CACHE_NAME));
        }
    }

    @Test
    public void exceptionThrowingCacheWhileReadingTest() throws InterruptedException {
        {
            Sciurus.registerCache(CUSTOM_CACHE_NAME, new CustomCache() {
                @Override
                public Object get(ExecutionIdentifier executionIdentifier) {
                    throw new RuntimeException("get: exceptionThrowingCacheTest");
                }

                @Override
                public void put(ExecutionIdentifier executionIdentifier, Serializable result, Duration duration) {
                }
            });
            new ToBeCached().cacheInCustomCache();
            checkExceptionWhileReading();
            assertTrue(Sciurus.deregisterCache(CUSTOM_CACHE_NAME));
        }
    }

    @Test
    public void exceptionThrowingCacheWhileStoringTest() throws InterruptedException {
        {
            Sciurus.registerCache(CUSTOM_CACHE_NAME, new CustomCache() {
                @Override
                public Object get(ExecutionIdentifier executionIdentifier) {
                    return EMPTY;
                }

                @Override
                public void put(ExecutionIdentifier executionIdentifier, Serializable result, Duration duration) {
                    throw new RuntimeException("put: exceptionThrowingCacheTest");
                }
            });
            new ToBeCached().cacheInCustomCache();
            checkExceptionWhileStoring();
            assertTrue(Sciurus.deregisterCache(CUSTOM_CACHE_NAME));
        }
    }

    @Test
    public void overrideGloablCache() throws InterruptedException {
        TestCache testCache = new TestCache();

        Sciurus.registerCache(Sciurus.CACHE_GLOBAL, testCache);
        new ToBeCached().cacheable();
        assertEquals(1, testCache.countAndClear());

        assertTrue(Sciurus.deregisterCache(Sciurus.CACHE_GLOBAL));
        new ToBeCached().cacheable();
        assertEquals(0, testCache.countAndClear());
    }

    @Test
    public void overrideMapCache() throws InterruptedException {
        TestCache testCache = new TestCache();

        Sciurus.registerCache(Sciurus.CACHE_MAP, testCache);
        assertEquals(12, new ToBeCached().cacheInMapCache());
        assertEquals(1, testCache.countAndClear());

        assertTrue(Sciurus.deregisterCache(Sciurus.CACHE_MAP));
        new ToBeCached().cacheInMapCache();
        assertEquals(0, testCache.countAndClear());
    }

    @Test
    public void cacheFor1SecondTest() throws InterruptedException {
        double result = new ToBeCached().cacheFor1Second();
        Thread.sleep(500);
        double newResult = new ToBeCached().cacheFor1Second();
        assertEquals(result, newResult, 0);
    }

    @Test
    public void unregisterNotExistingCache() {
        assertFalse(Sciurus.deregisterCache(null));
        assertFalse(Sciurus.deregisterCache(""));
        assertFalse(Sciurus.deregisterCache("asd"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration1() {
        Sciurus.registerCache(null, (CustomCache) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration2() {
        Sciurus.registerCache(null, (CacheSupplier) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration3() {
        Sciurus.registerCache("A", (CustomCache) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration4() {
        Sciurus.registerCache("A", (CacheSupplier) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration5() {
        Sciurus.registerCache("", new TestCache());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration6() {
        Sciurus.registerCache("", TestCache::new);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalConfiguration7() {
        Sciurus.registerCache(null, new TestCache());
    }

    @Test
    public void testStopCache() throws InterruptedException {
        {
            new ToBeCached().cacheable(); // Save in Cache

            stopCache();

            long before = System.currentTimeMillis();
            new ToBeCached().cacheable();
            long after = System.currentTimeMillis();

            checkNotCached(after - before);
        }
    }

    private void checkUnknownCacheException() {
        List<Map.Entry<Level, String>> logEntries = appender.getLogs().stream().filter(e -> e.getKey() == Level.WARN).collect(Collectors.toList());
        assertEquals(4, logEntries.size());
        logEntries.stream().map(Map.Entry::getValue).forEach(message -> assertThat(message, matchesPattern(".*No cache with name '" + CUSTOM_CACHE_NAME + "' registered.*")));
        appender.clear();
    }

    private void checkIllegalStateException() {
        List<Map.Entry<Level, String>> logEntries = appender.getLogs().stream().filter(e -> e.getKey() == Level.WARN).collect(Collectors.toList());
        assertEquals(2, logEntries.size());
        logEntries.stream().map(Map.Entry::getValue).forEach(message -> assertThat(message, matchesPattern(".*Cache returned from supplier for '" + CUSTOM_CACHE_NAME + "' was null.*")));
        appender.clear();
    }

    private void checkExceptionFromSupplier() {
        List<Map.Entry<Level, String>> logEntries = appender.getLogs().stream().filter(e -> e.getKey() == Level.WARN).collect(Collectors.toList());
        assertEquals(2, logEntries.size());
        logEntries.stream().map(Map.Entry::getValue).forEach(message -> assertThat(message, matchesPattern(".*Got exception from cache supplier for '" + CUSTOM_CACHE_NAME + "'.*")));
        appender.clear();
    }

    private void checkExceptionWhileReading() {
        List<Map.Entry<Level, String>> logEntries = appender.getLogs().stream().filter(e -> e.getKey() == Level.WARN).collect(Collectors.toList());
        assertEquals(1, logEntries.size());
        logEntries.stream().map(Map.Entry::getValue).forEach(message -> assertThat(message, matchesPattern(".*Got exception from cache '" + CUSTOM_CACHE_NAME + "' while reading.*")));
        appender.clear();
    }

    private void checkExceptionWhileStoring() {
        List<Map.Entry<Level, String>> logEntries = appender.getLogs().stream().filter(e -> e.getKey() == Level.WARN).collect(Collectors.toList());
        assertEquals(1, logEntries.size());
        logEntries.stream().map(Map.Entry::getValue).forEach(message -> assertThat(message, matchesPattern(".*Got exception from cache '" + CUSTOM_CACHE_NAME + "' while storing.*")));
        appender.clear();
    }

    private void checkNoUnknownCacheException() {
        assertEquals(0, appender.getMessages().size());
    }

    private void checkCached(long time) {
        assertThat(time, is(lessThan(CACHETIME / 2)));
    }

    private void checkNotCached(long time) {
        assertThat(time, is(greaterThan(CACHETIME - 5)));
    }

    static class TestCache implements CustomCache {

        private final Map<ExecutionIdentifier, Object> map = new HashMap<>();

        @Override
        public Object get(ExecutionIdentifier executionIdentifier) {
            return map.getOrDefault(executionIdentifier, EMPTY);
        }

        @Override
        public void put(ExecutionIdentifier executionIdentifier, Serializable result, Duration duration) {
            map.put(executionIdentifier, result);
        }

        int countAndClear() {
            int size = map.size();
            map.clear();
            return size;
        }
    }
}
