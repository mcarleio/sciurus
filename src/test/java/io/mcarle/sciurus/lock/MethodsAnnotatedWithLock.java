package io.mcarle.sciurus.lock;

import io.mcarle.sciurus.annotation.Lock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Marcel on 27.04.2016.
 */
public class MethodsAnnotatedWithLock {

    private static final Logger LOG = LogManager.getLogger(MethodsAnnotatedWithLock.class);

    @Lock
    public void ignoreParameters(String param) throws InterruptedException {
        LOG.info("{}:", param);
        Thread.sleep(100);
    }

    @Lock
    public void exceptionThrowing(String param, int i) throws InterruptedException {
        LOG.info("{}:", param);
        Thread.sleep(100);
        if (i % 2 == 0) {
            throw new RuntimeException(param);
        }
    }

    @Lock(on = {1, 3})
    public void ignoreSomeParameters(String param, int param1, int param2, int param3) throws InterruptedException {
        LOG.info("{}: {} - {} - {}", param, param1, param2, param3);
        Thread.sleep(500);
    }

    @Lock
    public void recursiveLock(String param, int i) throws InterruptedException {
        int start = i;
        while (i > 0) {
            LOG.info("{}: {} - {}", param, start, --i);
            recursiveLock(param, i);
            Thread.sleep(100);
        }
    }

    @Lock(on = {1})
    public void recursiveLockWithParameter(String param, int i) throws InterruptedException {
        int start = i;
        while (i > 0) {
            LOG.info("{}: {} - {}", param, start, --i);
            recursiveLockWithParameter(param, i);
            Thread.sleep(100);
        }
    }

    @Lock
    public void multiLockedMethod1(String param) throws InterruptedException {
        multiLockedMethod2(param);
    }

    @Lock
    private void multiLockedMethod2(String param) throws InterruptedException {
        LOG.info("{}", param);
        Thread.sleep(500);
    }

}

