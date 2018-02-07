package io.mcarle.sciurus;

import io.mcarle.sciurus.cache.CacheRegister;
import io.mcarle.sciurus.cache.CustomCache;
import io.mcarle.sciurus.monitor.MonitorRegister;
import io.mcarle.sciurus.monitor.CustomMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public final class Sciurus {

    private final static Logger LOG = LoggerFactory.getLogger(Sciurus.class);
    public final static String CACHE_GLOBAL = "CACHE_GLOBAL";
    public final static String CACHE_MAP = "CACHE_MAP";

    private static boolean MONITOR_STARTED = false;
    private static boolean CACHE_STARTED = false;

    public static boolean registerMonitor(CustomMonitor monitor) {
        LOG.trace("Register monitor {}", monitor);
        return MonitorRegister.INSTANCE.register(monitor);
    }

    public static boolean deregisterMonitor(CustomMonitor monitor) {
        LOG.trace("Deregister monitor {}", monitor);
        return MonitorRegister.INSTANCE.deregister(monitor);
    }

    public static void registerCache(String cacheName, CustomCache customCache) {
        LOG.trace("Register cache {}", cacheName);
        CacheRegister.INSTANCE.register(cacheName, customCache);
    }

    public static void registerCache(String cacheName, Supplier<CustomCache> customCacheSupplier) {
        LOG.trace("Register cache {}", cacheName);
        CacheRegister.INSTANCE.register(cacheName, customCacheSupplier);
    }

    public static boolean deregisterCache(String cacheName) {
        LOG.trace("Deregister cache {}", cacheName);
        return CacheRegister.INSTANCE.deregister(cacheName);
    }

    /*
     * start, stop, isStarted methods:
     */

    public static void startMonitor() {
        MONITOR_STARTED = true;
        LOG.debug("Monitor started");
    }

    public static void stopMonitor() {
        MONITOR_STARTED = false;
        LOG.debug("Monitor stopped");
    }

    public static void startCache() {
        CACHE_STARTED = true;
        LOG.debug("Cache started");
    }

    public static void stopCache() {
        CACHE_STARTED = false;
        LOG.debug("Cache stopped");
    }


    public static boolean isMonitorStarted() {
        return MONITOR_STARTED;
    }

    public static boolean isCacheStarted() {
        return CACHE_STARTED;
    }

}
