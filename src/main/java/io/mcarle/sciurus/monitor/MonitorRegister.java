package io.mcarle.sciurus.monitor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum MonitorRegister {
    INSTANCE;

    private static Set<CustomMonitor> MONITOR_REGISTER = Collections.synchronizedSet(
            new LinkedHashSet<>(
                    Collections.singletonList(
                            LoggingMonitor.INSTANCE // Add the LoggingMonitor as default.
                    )
            )
    );

    void notifyRegisteredMonitors(
            Duration duration,
            String declaringTypeName,
            Method method,
            Object[] methodArgs,
            Throwable throwable,
            Class returnType,
            Object returnValue
    ) {
        MONITOR_REGISTER.forEach(listener -> listener.monitored(
                duration, declaringTypeName, method, methodArgs, throwable, returnType, returnValue
        ));
    }

    public boolean register(CustomMonitor monitor) {
        return MONITOR_REGISTER.add(monitor);
    }

    public boolean deregister(CustomMonitor monitor) {
        return MONITOR_REGISTER.remove(monitor);
    }

}
