package io.mcarle.sciurus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a method is annotated with this annotation, the method execution will lock any other execution of this method,
 * until the execution finishes. With {@link #on()} you can define, that a method execution is only equal to another,
 * if the specified parameters have the same values.
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Lock {

    /**
     * The method parameter indices, starting with zero
     */
    int[] on() default {};
}
