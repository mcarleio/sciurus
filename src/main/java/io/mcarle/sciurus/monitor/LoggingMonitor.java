package io.mcarle.sciurus.monitor;

import io.mcarle.sciurus.annotation.LoggingMonitorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public enum LoggingMonitor implements CustomMonitor {
    INSTANCE;

    private final static Logger LOG = LoggerFactory.getLogger(LoggingMonitor.class);
    private final static String BASE_LOG_TEMPLATE = "({} {}) - {}#{}";
    private final static String LOG_TEMPLATE_PARAMS = "({})";
    private final static String LOG_TEMPLATE_RESULT = " : {}";

    @Override
    public void monitored(
            Duration duration,
            String declaringTypeName,
            Method method,
            Object[] methodArgs,
            Throwable throwable,
            Class returnType,
            Object returnValue
    ) {
        LoggingMonitorParams loggingParams = getLoggingMonitorParams(method);
        getLogger(loggingParams, duration)
                .accept(
                        extractLogTemplate(loggingParams),
                        extractLogParams(
                                loggingParams,
                                duration,
                                declaringTypeName,
                                method.getName(),
                                methodArgs,
                                extractMethodResult(throwable, returnType, returnValue)
                        )
                );
    }

    private LoggingMonitorParams getLoggingMonitorParams(Method method) {
        LoggingMonitorParams params = method.getAnnotation(LoggingMonitorParams.class);
        if (params == null) {
            params = method.getDeclaringClass().getAnnotation(LoggingMonitorParams.class);
            if (params == null) {
                params = LoggingMonitorParamsAnnotationClass.class.getAnnotation(LoggingMonitorParams.class);
            }
        }
        return params;
    }

    private static BiConsumer<String, Object[]> getLogger(final LoggingMonitorParams monitorParams, final Duration duration) {
        if (monitorParams.warnLimit() > 0) {
            if (Duration.of(monitorParams.warnLimit(), monitorParams.warnLimitUnit()).compareTo(duration) < 0) {
                return LOG::warn;
            }
        }
        return LOG::debug;
    }

    private static Object extractMethodResult(
            final Throwable throwable,
            final Class returnType,
            final Object returnValue
    ) {
        return throwable != null
                ? throwable.toString()
                : (returnType == Void.TYPE ? "void" : returnValue);
    }

    private static String extractLogTemplate(final LoggingMonitorParams monitorParams) {
        String result = BASE_LOG_TEMPLATE;
        if (monitorParams.logParameter()) {
            result += LOG_TEMPLATE_PARAMS;
        }
        if (monitorParams.logResult()) {
            result += LOG_TEMPLATE_RESULT;
        }
        return result;
    }

    private static Object[] extractLogParams(
            final LoggingMonitorParams monitorParams,
            final Duration duration,
            final String className,
            final String methodName,
            final Object[] methodArgs,
            final Object methodResult
    ) {
        ChronoUnit outputUnit = smallestUnitWithValueMaximal(duration, 9999);

        List<Object> result = new ArrayList<>(
                Arrays.asList(
                        String.format("%4s", getDurationIn(duration, outputUnit)),
                        abbreviationOfChronoUnit(outputUnit),
                        className,
                        methodName
                )
        );
        if (monitorParams.logParameter()) {
            result.add(methodArgs);
        }
        if (monitorParams.logResult()) {
            result.add(methodResult);
        }
        return result.toArray();
    }

    private static String abbreviationOfChronoUnit(ChronoUnit unit) {
        if (unit.equals(ChronoUnit.SECONDS)) {
            return "s ";
        } else if (unit.equals(ChronoUnit.MILLIS)) {
            return "ms";
        } else if (unit.equals(ChronoUnit.MICROS)) {
            return "µs";
        } else {
            return "ns";
        }
    }

    private static ChronoUnit smallestUnitWithValueMaximal(Duration duration, int maxVal) {
        return Stream.of(ChronoUnit.NANOS, ChronoUnit.MICROS, ChronoUnit.MILLIS, ChronoUnit.SECONDS)
                .filter(unit -> getDurationIn(duration, unit) < maxVal)
                .findFirst()
                .orElse(ChronoUnit.SECONDS);
    }

    private static long getDurationIn(Duration duration, ChronoUnit unit) {
        if (unit == ChronoUnit.MICROS) {
            return (duration.toNanos() + 500) / 1_000;
        } else if (unit == ChronoUnit.MILLIS) {
            return (duration.toNanos() + 500_000) / 1_000_000;
        } else if (unit == ChronoUnit.SECONDS) {
            return (duration.toNanos() + 500_000_000) / 1_000_000_000;
        } else {
            return duration.toNanos();
        }
    }

    @LoggingMonitorParams
    static class LoggingMonitorParamsAnnotationClass {
    }
}
