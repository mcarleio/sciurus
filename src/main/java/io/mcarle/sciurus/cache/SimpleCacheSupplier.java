package io.mcarle.sciurus.cache;

public class SimpleCacheSupplier implements CacheSupplier {

    private final CustomCache cache;

    public SimpleCacheSupplier(final CustomCache cache) {
        this.cache = cache;
    }

    @Override
    public CustomCache get() {
        return cache;
    }

    @Override
    public void preRegister() {
        cache.preRegister();
    }

    @Override
    public void postDeregister() {
        cache.postDeregister();
    }
}
