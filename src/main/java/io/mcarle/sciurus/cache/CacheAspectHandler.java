package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.ExecutionIdentifier;
import io.mcarle.sciurus.annotation.Cache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;

class CacheAspectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CacheAspectHandler.class);
    private final static Object EMPTY = CustomCache.EMPTY;

    @SuppressWarnings({"finally", "ReturnInsideFinallyBlock"})
    static Object executeAndCache(ProceedingJoinPoint joinPoint, Cache cache) throws Throwable {
        ExecutionIdentifier executionIdentifier = null;
        Object result = EMPTY;
        try {
            // check for any cached value
            executionIdentifier = new ExecutionIdentifier(joinPoint.getSignature().toLongString(), joinPoint.getArgs());
            result = loadFromCache(executionIdentifier, cache);
        } finally {
            if (result != EMPTY) {
                // if cached value was found, return it
                return result;
            }

            // if no cached value is present, execute method
            result = joinPoint.proceed();

            // if successfull
            try {
                // save the result into cache
                saveIntoCache(executionIdentifier, (Serializable) result, cache);
            } finally {
                // despite any errors while saving to cache, ignore them and return the result
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

    private static void saveIntoCache(ExecutionIdentifier executionIdentifier, Serializable result, Cache cache) {
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
        } catch (CacheException.UnknownCache ce) {
            LOG.warn("No cache with name '{}' registered: {}", cache.cacheName(), executionIdentifier);
        } catch (CacheException.CacheSupplierReturnedNull ce) {
            LOG.warn("Cache returned from supplier for '{}' was null: {}", cache.cacheName(), executionIdentifier);
        } catch (CacheException.CacheSupplierThrewException ce) {
            LOG.warn("Got exception from cache supplier for '{}': {}", cache.cacheName(), executionIdentifier, ce.getCause());
        }
        return null;
    }
}
