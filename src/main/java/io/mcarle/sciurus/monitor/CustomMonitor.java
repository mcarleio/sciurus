package io.mcarle.sciurus.monitor;

import java.lang.reflect.Method;
import java.time.Duration;

public interface CustomMonitor {

    void monitored(
          final Duration duration,
          final String declaringTypeName,
          final Method method,
          final Object[] methodArgs,
          final Throwable throwable,
          final Class returnType,
          final Object returnValue
    );

}