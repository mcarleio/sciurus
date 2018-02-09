package io.mcarle.example.sciurus.monitor.resources;

import io.mcarle.example.sciurus.monitor.analyzr.MonitorAnalyzer;
import io.mcarle.sciurus.annotation.Monitor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.time.Duration;

@Path("/monitor")
public class ExampleMonitorResource {

    private final Method longRunningProcessMethod = ExampleMonitorResource.class.getMethod("longRunningProcess", long.class);

    public ExampleMonitorResource() throws NoSuchMethodException {
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String waitFor(@QueryParam("wait") long waitTime) throws InterruptedException {
        if (waitTime <= 0) {
            waitTime = (long) (Math.random() * 1500);
        }
        longRunningProcess(waitTime);

        Duration curDuration = MonitorAnalyzer.INSTANCE.lastOf(longRunningProcessMethod);
        Duration maxDuration = MonitorAnalyzer.INSTANCE.maxOf(longRunningProcessMethod);
        Duration minDuration = MonitorAnalyzer.INSTANCE.minOf(longRunningProcessMethod);
        Duration avgDuration = MonitorAnalyzer.INSTANCE.averageOf(longRunningProcessMethod);

        return String.format(
                "Cur execution time: %s\nMax execution time: %s\nMin execution time: %s\nAvg execution time: %s",
                curDuration,
                maxDuration,
                minDuration,
                avgDuration
        );
    }

    @Monitor
    public void longRunningProcess(long waitTime) throws InterruptedException {
        Thread.sleep(waitTime);
    }
}