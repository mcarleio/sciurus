package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.Sciurus;
import io.mcarle.sciurus.annotation.Cache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CacheAspect {

    /**
     * Matches any execution of any method returning a serializable result
     */
    @Pointcut("execution((java.io.Serializable+ || byte || short || int || long || float || double || char || boolean) *(..))")
    void anyMethodReturningSerializable() {
    }

    /**
     * Matches any execution of any method with at least one non serializable parameter
     */
    @Pointcut("execution(* *(.., !(java.io.Serializable+ || byte || short || int || long || float || double || char || boolean), ..))")
    void anyMethodWithMinimumOneNonSerializableParameter() {
    }

    /**
     * Matches any method, which is annotated with {@link Cache}
     */
    @Pointcut("@annotation(cache)")
    void cacheAnnotated(Cache cache) {
    }

    @Around(value = "anyMethodReturningSerializable() && !anyMethodWithMinimumOneNonSerializableParameter() && cacheAnnotated(cache)")
    public Object startedAndExecutionOfAnyMethodAnnotatedWithCache(
            ProceedingJoinPoint joinPoint,
            Cache cache
    ) throws Throwable {
        if (Sciurus.isCacheStarted()) {
            return CacheAspectHandler.executeAndCache(joinPoint, cache);
        } else {
            return joinPoint.proceed();
        }
    }
}
