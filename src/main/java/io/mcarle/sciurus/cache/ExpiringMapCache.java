package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.ExecutionIdentifier;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public enum ExpiringMapCache implements CustomCache {

    INSTANCE;

    private final ExpiringMap<ExecutionIdentifier, Object> MAP = ExpiringMap.builder().variableExpiration().build();

    @Override
    public Object get(ExecutionIdentifier executionIdentifier) {
        if (MAP.containsKey(executionIdentifier)) {
            return MAP.get(executionIdentifier);
        }
        return EMPTY;
    }

    @Override
    public void put(ExecutionIdentifier executionIdentifier, Serializable result, Duration duration) {
        MAP.put(
                executionIdentifier,
                result,
                ExpirationPolicy.CREATED,
                duration.getSeconds() * 1_000_000_000 + duration.getNano(),
                TimeUnit.NANOSECONDS
        );
    }
}
