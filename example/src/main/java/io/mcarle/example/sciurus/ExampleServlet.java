package io.mcarle.example.sciurus;

import io.mcarle.example.sciurus.cache.mbean.CacheSwitcher;
import io.mcarle.example.sciurus.monitor.analyzr.MonitorAnalyzer;
import io.mcarle.example.sciurus.monitor.mbean.MonitorSwitcher;
import io.mcarle.sciurus.Sciurus;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.management.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import java.lang.management.ManagementFactory;

@WebServlet(urlPatterns = "/*", name = "ExampleServlet", loadOnStartup = 1, initParams = {
        @WebInitParam(name = "jersey.config.server.provider.packages", value = "io.mcarle.example.sciurus.monitor.resources;io.mcarle.example.sciurus.cache.resources;io.mcarle.example.sciurus.lock.resources")
})
public class ExampleServlet extends ServletContainer {

    @Override
    public void destroy() {
        super.destroy();
        Sciurus.stopMonitor();
        Sciurus.stopCache();
    }

    @Override
    public void init() throws ServletException {
        Sciurus.startMonitor();
        Sciurus.startCache();
        Sciurus.registerMonitor(
                (duration, declaringTypeName, method, methodArgs, throwable, returnType, returnValue) -> MonitorAnalyzer.INSTANCE.addMethodExecution(method, duration)
        );

        try {
            ObjectName name;
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

            name = new ObjectName(ExampleServlet.class.getPackage().getName() + ":type=" + MonitorSwitcher.class.getSimpleName());
            mbs.registerMBean(new MonitorSwitcher(), name);

            name = new ObjectName(ExampleServlet.class.getPackage().getName() + ":type=" + CacheSwitcher.class.getSimpleName());
            mbs.registerMBean(new CacheSwitcher(), name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.init();
    }
}
