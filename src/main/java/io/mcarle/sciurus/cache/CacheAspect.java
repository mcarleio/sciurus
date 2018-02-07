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
     * Checks, if cache of Sciurus is started
     *
     * @return The state of sciurus' cache: {@code true} if it is started, {@code false} otherwise.
     */
    @Pointcut("if()")
    public static boolean isCacheStarted() {
        return Sciurus.isCacheStarted();
    }

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

    @Around("isCacheStarted() && anyMethodReturningSerializable() && !anyMethodWithMinimumOneNonSerializableParameter() && cacheAnnotated(cache)")
    public Object startedAndExecutionOfAnyMethodAnnotatedWithCache(
          ProceedingJoinPoint joinPoint,
          Cache cache
    ) throws Throwable {
        return CacheAspectHandler.executeAndCache(joinPoint, cache);
    }
}
