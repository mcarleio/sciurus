package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.annotation.Cache;
import io.mcarle.sciurus.ExecutionIdentifier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

class CacheAspectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CacheAspectHandler.class);
    private final static Object EMPTY = CustomCache.EMPTY;

    static Object executeAndCache(ProceedingJoinPoint joinPoint, Cache cache) throws Throwable {
        ExecutionIdentifier executionIdentifier = null;
        Object result = EMPTY;
        try {
            executionIdentifier = new ExecutionIdentifier(joinPoint.getSignature().toLongString(), joinPoint.getArgs());
            result = loadFromCache(executionIdentifier, cache);
        } finally {
            if (result != EMPTY) {
                return result;
            }

            result = joinPoint.proceed();

            try {
                saveIntoCache(executionIdentifier, result, cache);
            } finally {
                return result;
            }
        }
    }


    private static Object loadFromCache(ExecutionIdentifier executionIdentifier, Cache cache) {
        CustomCache customCache = getCache(cache, executionIdentifier);
        if (customCache != null) {
            try {
                return customCache.get(executionIdentifier);
            } catch (Throwable t) {
                LOG.warn("Got exception from cache '{}' while reading: {}", cache.cacheName(), executionIdentifier, t);
            }
        }
        return EMPTY;
    }

    private static void saveIntoCache(ExecutionIdentifier executionIdentifier, Object result, Cache cache) {
        CustomCache customCache = getCache(cache, executionIdentifier);
        if (customCache != null) {
            try {
                customCache.put(executionIdentifier, result, Duration.of(cache.time(), cache.unit()));
            } catch (Throwable t) {
                LOG.warn("Got exception from cache '{}' while storing: {}", cache.cacheName(), executionIdentifier, t);
            }
        }
    }

    private static CustomCache getCache(Cache cache, ExecutionIdentifier executionIdentifier) {
        try {
            return CacheRegister.INSTANCE.getCache(cache.cacheName());
        } catch (UnknownCacheException uce) {
            LOG.warn("No cache with name '{}' registered: {}", cache.cacheName(), executionIdentifier);
        } catch (IllegalStateException ise) {
            LOG.warn("Cache returned from supplier for '{}' was null: {}", cache.cacheName(), executionIdentifier);
        } catch (Throwable t) {
            LOG.warn("Got exception from cache supplier for '{}': {}", cache.cacheName(), executionIdentifier, t);
        }
        return null;
    }
}
