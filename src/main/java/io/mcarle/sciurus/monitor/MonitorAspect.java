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

    @Around(value = "anyMethod() && monitorAnnotated(monitor)", argNames = "joinPoint,monitor")
    public Object startedAndExecutionOfAnyMethodAnnotatedWithMonitor(
            ProceedingJoinPoint joinPoint,
            Monitor monitor
    ) throws Throwable {
        if (Sciurus.isMonitorStarted()) {
            return MonitorAspectHandler.executeAndMeasure(joinPoint);
        } else {
            return joinPoint.proceed();
        }
    }

}
