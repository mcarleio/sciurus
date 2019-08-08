package io.mcarle.sciurus.retry;

import io.mcarle.sciurus.annotation.Retry;
import org.aspectj.lang.ProceedingJoinPoint;

class RetryAspectHandler {

    static Object executeAndRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        int retryCount = 0;
        while (true) {
            try {
                return joinPoint.proceed(); // when no exception, return result
            } catch (Throwable t) {
                if (retryCount >= retry.times()) {
                    // when not succeeded after specified retry amount, throw last exception
                    throw t;
                }
                retryCount++;
            }
        }
    }
}
