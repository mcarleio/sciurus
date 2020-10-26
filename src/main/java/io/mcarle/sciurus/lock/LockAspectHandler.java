package io.mcarle.sciurus.lock;

import io.mcarle.sciurus.ExecutionIdentifier;
import io.mcarle.sciurus.annotation.Lock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class LockAspectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LockAspectHandler.class);
    private static final Map<ExecutionIdentifier, Execution> ACTIVE_EXECUTIONS = Collections.synchronizedMap(new HashMap<>());
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock(true);

    static Object checkLockAndExecute(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        Execution execution = new Execution(joinPoint, lock);
        return checkLockAndExecute(execution);
    }

    private static Object checkLockAndExecute(Execution exec) throws Throwable {
        Execution oldExecution = ACTIVE_EXECUTIONS.putIfAbsent(exec.getId(), exec);
        if (oldExecution == null || oldExecution.getActivator().get() == Boolean.TRUE) {
            LOG.trace("Create lock: {}", exec);
            synchronized (exec.getLock()) {
                try {
                    exec.getActivator().set(Boolean.FALSE);
                    return exec.getJoinPoint().proceed();
                } finally {
                    exec.getWaitingThreads().remove(Thread.currentThread().getId());
                    REENTRANT_LOCK.lock();
                    if (exec.getWaitingThreads().isEmpty()) {
                        LOG.trace("Remove finished thread from waiting thread map: {}", exec);
                        ACTIVE_EXECUTIONS.remove(exec.getId());
                    }
                    REENTRANT_LOCK.unlock();
                    LOG.trace("Release lock: {}", exec);
                    exec.getLock().notifyAll();
                }
            }
        } else {
            REENTRANT_LOCK.lock();
            Execution lockedExec = ACTIVE_EXECUTIONS.get(exec.getId());
            if (lockedExec != null) {
                if (lockedExec.getActivator().get() == null) {
                    lockedExec.getWaitingThreads().add(Thread.currentThread().getId());
                    REENTRANT_LOCK.unlock();
                    LOG.trace("Wait for lock release: {}", lockedExec);
                    synchronized (lockedExec.getLock()) {
                        while (lockedExec.getWaitingThreads().indexOf(Thread.currentThread().getId()) > 0) {
                            lockedExec.getLock().wait(100);
                            LOG.trace("Wait released or reached timeout: {}", lockedExec);
                        }
                        lockedExec.setJoinPointAndActivate(exec.getJoinPoint());
                    }
                    LOG.trace("Got lock release: {}", lockedExec);
                    return checkLockAndExecute(lockedExec);
                } else {
                    REENTRANT_LOCK.unlock();
                    LOG.trace("Recursive call, proceed: {}", lockedExec);
                    return exec.getJoinPoint().proceed();
                }
            } else {
                /*
                 * Should only happen when oldExecution was still running, but until getting the lockedExec,
                 * that thread finished its work and removed itself from the active execution map. (i.e. rarely)
                 */
                REENTRANT_LOCK.unlock();
                LOG.trace("No active execution found, retry: {}", exec);
                return checkLockAndExecute(exec);
            }
        }
    }
}
