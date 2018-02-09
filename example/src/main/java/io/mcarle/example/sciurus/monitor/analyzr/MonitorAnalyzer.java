package io.mcarle.example.sciurus.monitor.analyzr;


import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public enum MonitorAnalyzer {
    INSTANCE;

    private Map<Method, List<Duration>> methodExecutionDurationList = new HashMap<>();

    public void addMethodExecution(Method method, Duration duration) {
        List<Duration> durationList = methodExecutionDurationList.computeIfAbsent(method, k -> new LinkedList<>());
        durationList.add(duration);
    }

    public Duration averageOf(String methodName) {
        return averageOf(getDurationList(methodName));
    }

    public Duration averageOf(Method method) {
        return averageOf(methodExecutionDurationList.get(method));
    }

    private Duration averageOf(List<Duration> durationList) {
        if (durationList == null) {
            return null;
        }
        return getAvgExecutionDuration(durationList);
    }

    public Duration maxOf(String methodName) {
        return maxOf(getDurationList(methodName));
    }

    public Duration maxOf(Method method) {
        return maxOf(methodExecutionDurationList.get(method));
    }

    private Duration maxOf(List<Duration> durationList) {
        if (durationList == null) {
            return null;
        }
        return durationList
                .stream()
                .max(Duration::compareTo)
                .orElse(null);
    }

    public Duration minOf(String methodName) {
        return minOf(getDurationList(methodName));
    }

    public Duration minOf(Method method) {
        return minOf(methodExecutionDurationList.get(method));
    }

    private Duration minOf(List<Duration> durationList) {
        if (durationList == null) {
            return null;
        }
        return durationList
                .stream()
                .min(Duration::compareTo)
                .orElse(null);
    }

    public Duration lastOf(String methodName) {
        return lastOf(getDurationList(methodName));
    }

    public Duration lastOf(Method method) {
        return lastOf(methodExecutionDurationList.get(method));
    }

    private Duration lastOf(List<Duration> durationList) {
        if (durationList == null) {
            return null;
        }
        return durationList
                .stream()
                .reduce(Duration.ZERO, (first, second) -> second);
    }

    private List<Duration> getDurationList(String methodName) {
        return methodExecutionDurationList.keySet().stream().filter(m -> m.getName().equalsIgnoreCase(methodName)).map(methodExecutionDurationList::get).findFirst().orElse(new LinkedList<>());
    }

    private Duration getAvgExecutionDuration(Collection<Duration> durations) {
        durations = Collections.unmodifiableCollection(durations);
        long resultNanos = 0;
        int elements = durations.size();
        for (Duration duration : durations) {
            resultNanos += (duration.get(ChronoUnit.NANOS) + duration.get(ChronoUnit.SECONDS) * 1_000_000_000) / elements;
        }
        return Duration.of(resultNanos, ChronoUnit.NANOS);
    }
}
