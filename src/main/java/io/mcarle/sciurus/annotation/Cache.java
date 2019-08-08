package io.mcarle.sciurus.annotation;

import io.mcarle.sciurus.Sciurus;
import io.mcarle.sciurus.cache.CustomCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * When a method is annotated with this annotation, the result of the method execution will be stored in the specified cache
 * for the specified {@link #time()}.
 * If you need different caches (e.g. when some data should be stored in external tools like Redis), you can register the
 * cache with {@link Sciurus#registerCache(String, CustomCache)} and a custom cache name.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    /**
     * Name of the target cache
     */
    String cacheName() default Sciurus.CACHE_GLOBAL;

    /**
     * Amount of time, for which the cached values should be stored
     */
    long time();

    /**
     * Unit for the time
     */
    ChronoUnit unit() default ChronoUnit.MILLIS;
}
