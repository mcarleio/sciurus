package io.mcarle.sciurus.lock;

import io.mcarle.sciurus.ExecutionIdentifier;
import io.mcarle.sciurus.annotation.Lock;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Execution {
    private final ExecutionIdentifier id;
    private final Object lock;
    private final ThreadLocal<Boolean> activator;
    private final List<Long> waitingThreads;
    private ProceedingJoinPoint joinPoint;

    Execution(ProceedingJoinPoint joinPoint, Lock lock) {
        this.joinPoint = joinPoint;
        this.lock = new Object();
        this.activator = new ThreadLocal<>();
        this.activator.set(Boolean.FALSE);
        this.waitingThreads = Collections.synchronizedList(new ArrayList<>());
        this.waitingThreads.add(Thread.currentThread().getId());
        List<Object> tmp = new ArrayList<>();
        for (int i : lock.on()) {
            tmp.add(joinPoint.getArgs()[i]);
        }
        this.id = new ExecutionIdentifier(joinPoint.getSignature().toString(), tmp.toArray());
    }

    public void setJoinPointAndActivate(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
        this.activator.set(Boolean.TRUE);
    }

    public ExecutionIdentifier getId() {
        return id;
    }

    public Object getLock() {
        return lock;
    }

    public ThreadLocal<Boolean> getActivator() {
        return activator;
    }

    public List<Long> getWaitingThreads() {
        return waitingThreads;
    }

    public ProceedingJoinPoint getJoinPoint() {
        return joinPoint;
    }

    @Override
    public String toString() {
        return "ID: " + id + " - Waiting: " + waitingThreads + " - Active? " + (activator.get() != null);
    }
}
