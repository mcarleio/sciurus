package io.mcarle.example.sciurus.monitor.mbean;

import io.mcarle.sciurus.Sciurus;

public class MonitorSwitcher implements MonitorSwitcherMBean {
    @Override
    public void startMonitor() {
        Sciurus.startMonitor();
    }

    @Override
    public void stopMonitor() {
        Sciurus.stopMonitor();
    }
}
