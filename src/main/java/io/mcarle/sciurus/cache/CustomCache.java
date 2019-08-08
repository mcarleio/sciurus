package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.ExecutionIdentifier;

import java.io.Serializable;
import java.time.Duration;

public interface CustomCache {

    Object EMPTY = new Object();

    Object get(ExecutionIdentifier executionIdentifier);

    void put(ExecutionIdentifier executionIdentifier, Serializable result, Duration duration);

    default void preRegister() {
    }

    default void postDeregister() {
    }

}
