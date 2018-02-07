package io.mcarle.sciurus.monitor;

import io.mcarle.sciurus.annotation.LoggingMonitorParams;
import io.mcarle.sciurus.annotation.Monitor;

@LoggingMonitorParams(
        warnLimit = 20
)
public class AnnotatedToBeMonitored {

    @Monitor
    public void warnIfTooLong(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
