package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.Sciurus;
import io.mcarle.sciurus.annotation.Cache;

import java.time.temporal.ChronoUnit;

import static io.mcarle.sciurus.cache.CacheTest.CACHETIME;
import static io.mcarle.sciurus.cache.CacheTest.CUSTOM_CACHE_NAME;

public class ToBeCached {

    @Cache(time = CACHETIME)
    public void notCacheableBecauseOfVoid() throws InterruptedException {
        Thread.sleep(CACHETIME);
    }

    @Cache(time = CACHETIME)
    public void notCacheableBecauseOfVoid(String s) throws InterruptedException {
        Thread.sleep(CACHETIME);
    }

    @Cache(time = CACHETIME)
    public void notCacheableBecauseOfVoid(Object o) throws InterruptedException {
        Thread.sleep(CACHETIME);
    }

    @Cache(time = CACHETIME)
    public Object notCacheableBecauseOfReturnTypeNotSerializable() throws InterruptedException {
        Thread.sleep(CACHETIME);
        return new Object();
    }

    @Cache(time = CACHETIME)
    public Object notCacheableBecauseOfReturnTypeNotSerializable(String s) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return new Object();
    }

    @Cache(time = CACHETIME)
    public Object notCacheableBecauseOfReturnTypeNotSerializable(Object o) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return new Object();
    }

    @Cache(time = CACHETIME)
    public int notCacheableBecauseOfParameterTypeNotSerializable(Object o) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return 42;
    }

    @Cache(time = CACHETIME)
    public int notCacheableBecauseOfParameterTypeNotSerializable(int i, Object o) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return 42;
    }

    @Cache(time = CACHETIME)
    public int cacheable() throws InterruptedException {
        Thread.sleep(CACHETIME);
        return 42;
    }

    @Cache(time = CACHETIME)
    public int cacheable(int i) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return i;
    }

    @Cache(time = CACHETIME)
    public String cacheable(String s) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return s;
    }

    @Cache(time = CACHETIME)
    public long cacheable(String s, int i) throws InterruptedException {
        Thread.sleep(CACHETIME);
        return i + s.length();
    }

    @Cache(time = CACHETIME, cacheName = CUSTOM_CACHE_NAME)
    public long cacheInCustomCache() throws InterruptedException {
        Thread.sleep(CACHETIME);
        return 42;
    }

    @Cache(time = CACHETIME, cacheName = Sciurus.CACHE_MAP)
    public byte cacheInMapCache() throws InterruptedException {
        Thread.sleep(CACHETIME);
        return 12;
    }

    @Cache(time = 1, unit = ChronoUnit.SECONDS)
    public double cacheFor1Second() throws InterruptedException {
        Thread.sleep(CACHETIME);
        return Math.random();
    }

}
