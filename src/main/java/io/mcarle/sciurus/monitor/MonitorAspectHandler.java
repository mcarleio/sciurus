package io.mcarle.sciurus.monitor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class MonitorAspectHandler {

    static Object executeAndMeasure(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Object returnValue = null;
        Throwable throwable = null;
        long duration = 0;
        long before = System.nanoTime();
        try {
            returnValue = joinPoint.proceed();
            duration = System.nanoTime() - before;
        } catch (Throwable t) {
            duration = System.nanoTime() - before;
            throwable = t;
            throw t;
        } finally {
            MonitorRegister.INSTANCE.notifyRegisteredMonitors(
                  Duration.of(duration, ChronoUnit.NANOS),
                  signature.getDeclaringTypeName(),
                  signature.getMethod(),
                  joinPoint.getArgs(),
                  throwable,
                  signature.getReturnType(),
                  returnValue
            );
        }
        return returnValue;
    }
}
