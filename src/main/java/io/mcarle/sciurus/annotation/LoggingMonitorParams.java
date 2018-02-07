package io.mcarle.sciurus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggingMonitorParams {

    int warnLimit() default 0;

    ChronoUnit warnLimitUnit() default ChronoUnit.MILLIS;

    boolean logParameter() default false;

    boolean logResult() default false;

}
