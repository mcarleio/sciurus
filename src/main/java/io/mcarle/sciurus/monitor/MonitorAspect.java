package io.mcarle.sciurus.monitor;

import io.mcarle.sciurus.Sciurus;
import io.mcarle.sciurus.annotation.Monitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MonitorAspect {

    /**
     * Checks, if monitor of Sciurus is started
     *
     * @return The state of sciurus' monitor: {@code true} if it is started, {@code false} otherwise.
     */
    @Pointcut("if()")
    public static boolean isMonitorStarted() {
        return Sciurus.isMonitorStarted();
    }

    /**
     * Matches any execution of any method
     */
    @Pointcut("execution(* *(..))")
    void anyMethod() {
    }

    /**
     * Matches any method, which is annotated with {@link Monitor}
     */
    @Pointcut("@annotation(monitor)")
    void monitorAnnotated(Monitor monitor) {
    }

    @Around("isMonitorStarted() && anyMethod() && monitorAnnotated(monitor)")
    public Object startedAndExecutionOfAnyMethodAnnotatedWithMonitor(
          ProceedingJoinPoint joinPoint,
          Monitor monitor
    ) throws Throwable {
        return MonitorAspectHandler.executeAndMeasure(joinPoint);
    }

}
