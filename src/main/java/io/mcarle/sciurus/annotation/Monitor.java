package io.mcarle.sciurus.annotation;

import io.mcarle.sciurus.monitor.CustomMonitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a method is annotated with this annotation, each method execution will be measured for execution time and all parameters,
 * measurements and the result of each method execution will be send to a {@link io.mcarle.sciurus.monitor.CustomMonitor}.
 * If you do not have defined a custom monitor ({@link io.mcarle.sciurus.Sciurus#registerMonitor(CustomMonitor)}), the default
 * implementation {@link io.mcarle.sciurus.monitor.LoggingMonitor} will log some infos.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitor {

}
