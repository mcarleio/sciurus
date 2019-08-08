package io.mcarle.sciurus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a method is annotated with this annotation and the method throws an exception,
 * Sciurus will retry to execute the method until either it succeeds, or the specified
 * retry amount is reached.
 * When the method does not succeed, throw the exception from the latest method execution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

    /**
     * Amount of retries. 0 and less means no retry.
     */
    int times() default 1;
}
