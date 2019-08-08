package io.mcarle.sciurus.cache;

public interface CacheSupplier {

    CustomCache get();

    default void preRegister() {
    }

    default void postDeregister() {
    }
}
