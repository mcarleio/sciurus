package io.mcarle.sciurus.lock;

import io.mcarle.sciurus.annotation.Lock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LockAspect {

    /**
     * Matches any execution of any method
     */
    @Pointcut("execution(* *(..))")
    void anyMethod() {
    }

    /**
     * Matches any method, which is annotated with {@link Lock}
     */
    @Pointcut("@annotation(lock)")
    void lockAnnotated(Lock lock) {
    }

    @Around(value = "anyMethod() && lockAnnotated(lock)", argNames = "joinPoint,lock")
    public Object executionOfAnyMethodAnnotatedWithLock(
            ProceedingJoinPoint joinPoint,
            Lock lock
    ) throws Throwable {
        return LockAspectHandler.checkLockAndExecute(joinPoint, lock);
    }
}
