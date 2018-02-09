package io.mcarle.example.sciurus.cache.mbean;

import io.mcarle.sciurus.Sciurus;

public class CacheSwitcher implements CacheSwitcherMBean {
    @Override
    public void startCache() {
        Sciurus.startCache();
    }

    @Override
    public void stopCache() {
        Sciurus.stopCache();
    }
}
