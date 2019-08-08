package io.mcarle.sciurus.retry;

import io.mcarle.sciurus.annotation.Retry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class RetryAspect {

    /**
     * Matches any execution of any method
     */
    @Pointcut("execution(* *(..))")
    void anyMethod() {
    }

    /**
     * Matches any method, which is annotated with {@link Retry}
     */
    @Pointcut("@annotation(retry)")
    void retryAnnotated(Retry retry) {
    }

    @Around("anyMethod() && retryAnnotated(retry)")
    public Object executionOfAnyMethodAnnotatedWithRetry(
            ProceedingJoinPoint joinPoint,
            Retry retry
    ) throws Throwable {
        return RetryAspectHandler.executeAndRetry(joinPoint, retry);
    }
}
