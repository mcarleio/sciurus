package io.mcarle.sciurus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Only used by {@link io.mcarle.sciurus.monitor.LoggingMonitor} to identify:
 * <ol>
 * <li>1. if the log message should contain the parameters and the result of the monitored method and</li>
 * <li>2. if the execution time is unusual high and the log level should be {@link org.slf4j.Logger#warn}
 * instead of {@link org.slf4j.Logger#debug}</li>
 * </ol>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggingMonitorParams {

    int warnLimit() default 0;

    ChronoUnit warnLimitUnit() default ChronoUnit.MILLIS;

    boolean logParameter() default false;

    boolean logResult() default false;

}
