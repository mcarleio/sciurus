package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.Sciurus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum CacheRegister {

    INSTANCE;

    private final Supplier<CustomCache> DEFAULT_SUPPLIER = () -> ExpiringMapCache.INSTANCE;
    private final Map<String, Supplier<CustomCache>> CACHE_REGISTER = Collections.synchronizedMap(new HashMap<String, Supplier<CustomCache>>() {
        {
            put(Sciurus.CACHE_GLOBAL, DEFAULT_SUPPLIER);
            put(Sciurus.CACHE_MAP, DEFAULT_SUPPLIER);
        }
    });

    public void register(String cacheName, CustomCache cache) {
        if (cache == null) throw new IllegalArgumentException("missing cache");
        register(cacheName, () -> cache);
    }

    public void register(String cacheName, Supplier<CustomCache> cacheSupplier) {
        if (cacheSupplier == null) throw new IllegalArgumentException("missing cache supplier");
        if (cacheName == null || cacheName.isEmpty()) throw new IllegalArgumentException("missing or empty cache name");
        CACHE_REGISTER.put(cacheName, cacheSupplier);
    }

    public boolean deregister(String cacheName) {
        if (Sciurus.CACHE_GLOBAL.equals(cacheName) || Sciurus.CACHE_MAP.equals(cacheName)) {
            CACHE_REGISTER.put(cacheName, DEFAULT_SUPPLIER);
            return true;
        }
        return CACHE_REGISTER.remove(cacheName) != null;
    }

    CustomCache getCache(String cacheName) {
        Supplier<CustomCache> cacheSupplier = CACHE_REGISTER.get(cacheName);
        if (cacheSupplier == null) throw new UnknownCacheException(cacheName);

        CustomCache cache = cacheSupplier.get();
        if (cache == null) throw new IllegalStateException("CacheSupplier returned null");

        return cache;
    }

}
