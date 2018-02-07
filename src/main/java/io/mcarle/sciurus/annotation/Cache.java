package io.mcarle.sciurus.annotation;

import io.mcarle.sciurus.Sciurus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    String cacheName() default Sciurus.CACHE_GLOBAL;

    long time();

    ChronoUnit unit() default ChronoUnit.MILLIS;
}
