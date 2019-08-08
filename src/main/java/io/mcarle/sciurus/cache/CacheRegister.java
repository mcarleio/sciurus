package io.mcarle.sciurus.cache;

import io.mcarle.sciurus.Sciurus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum CacheRegister {

    INSTANCE;

    private final CacheSupplier DEFAULT_SUPPLIER = () -> ExpiringMapCache.INSTANCE;
    private final Map<String, CacheSupplier> CACHE_REGISTER = Collections.synchronizedMap(new HashMap<String, CacheSupplier>() {
        {
            put(Sciurus.CACHE_GLOBAL, DEFAULT_SUPPLIER);
            put(Sciurus.CACHE_MAP, DEFAULT_SUPPLIER);
        }
    });

    public void register(String cacheName, CustomCache cache) {
        if (cache == null) throw new IllegalArgumentException("missing cache");
        register(cacheName, new SimpleCacheSupplier(cache));
    }

    public void register(String cacheName, CacheSupplier cacheSupplier) {
        if (cacheSupplier == null) throw new IllegalArgumentException("missing cache supplier");
        if (cacheName == null || cacheName.isEmpty()) throw new IllegalArgumentException("missing or empty cache name");
        cacheSupplier.preRegister();
        CACHE_REGISTER.put(cacheName, cacheSupplier);
    }

    public boolean deregister(String cacheName) {
        CacheSupplier cacheSupplier = deregisterCache(cacheName);
        if (cacheSupplier != null) {
            cacheSupplier.postDeregister();
        }
        return cacheSupplier != null;
    }

    CustomCache getCache(String cacheName) throws CacheException {
        CacheSupplier cacheSupplier = CACHE_REGISTER.get(cacheName);
        if (cacheSupplier == null) throw new CacheException.UnknownCache();

        CustomCache cache;
        try {
            cache = cacheSupplier.get();
        } catch (Throwable t) {
            throw new CacheException.CacheSupplierThrewException(t);
        }

        if (cache == null) throw new CacheException.CacheSupplierReturnedNull();

        return cache;
    }


    private CacheSupplier deregisterCache(String cacheName) {
        if (Sciurus.CACHE_GLOBAL.equals(cacheName) || Sciurus.CACHE_MAP.equals(cacheName)) {
            return CACHE_REGISTER.put(cacheName, DEFAULT_SUPPLIER);
        } else {
            return CACHE_REGISTER.remove(cacheName);
        }
    }

}
