package io.mcarle.sciurus.lock;

import io.mcarle.sciurus.log4j.TestDateAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class LockTest {

    private ExecutorService executor;
    private Long LAST_VISIT = 0L;
    private final List<Integer> ORDER_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final String CREATE_RUNNABLE_EXCEPTION_THROWING = "createRunnableExceptionThrowing";
    private static final String CREATE_RUNNABLE_IGNORE_PARAMETERS = "createRunnableIgnoreParameters";
    private static final String CREATE_RUNNABLE_WITH_PARAMETERS = "createRunnableWithParameters";
    private static final String CREATE_RUNNABLE_RECURSIVE_LOCK = "createRunnableRecursiveLock";
    private static final String CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER = "createRunnableRecursiveLockWithParameter";
    private static final String CREATE_RUNNABLE_MULTI_LOCKED_METHOD = "createRunnableMultiLockedMethod1";

    private TestDateAppender appender;

    @Before
    public void cleanAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        appender = (TestDateAppender) config.getAppenders().get(TestDateAppender.NAME);

        appender.clear();
    }

    @Before
    public void before() {
        executor = Executors.newFixedThreadPool(12);
        ORDER_LIST.clear();
        LAST_VISIT = 0L;
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    public LockTest() {
    }

    @Test
    public void forBetterCodeCoverage() {
        new LockAspectHandler();
    }

    @Test
    public void exceptionThrowingTest() throws InterruptedException {
        List<Future<?>> results = new ArrayList<>();

        results.add(executor.submit(createRunnableExceptionThrowing(0)));
        results.add(executor.submit(createRunnableExceptionThrowing(1)));
        results.add(executor.submit(createRunnableExceptionThrowing(2)));
        results.add(executor.submit(createRunnableExceptionThrowing(3)));
        results.add(executor.submit(createRunnableExceptionThrowing(4)));
        results.add(executor.submit(createRunnableExceptionThrowing(5)));

        executor.shutdown();
        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));

        for (int i = 0; i < 6; i++) {
            Throwable exception = null;
            try {
                results.get(i).get();
            } catch (ExecutionException | InterruptedException e) {
                exception = e.getCause().getCause();
            }
            if (i % 2 == 0) {
                if (exception != null) {
                    assertEquals(CREATE_RUNNABLE_EXCEPTION_THROWING + i, exception.getMessage());
                } else {
                    fail("Fehlende Exception bei " + CREATE_RUNNABLE_EXCEPTION_THROWING + i);
                }
            } else {
                if (exception != null) {
                    fail("Unerwartete Exception bei " + CREATE_RUNNABLE_EXCEPTION_THROWING + i);
                }
            }

        }
    }

    @Test
    public void ignoreParametersTest() throws InterruptedException {
        executor.submit(createRunnableIgnoreParameters(0));
        executor.submit(createRunnableIgnoreParameters(0));
        executor.submit(createRunnableIgnoreParameters(1));
        executor.submit(createRunnableIgnoreParameters(1));
        executor.submit(createRunnableIgnoreParameters(2));
        executor.submit(createRunnableIgnoreParameters(2));
        executor.submit(createRunnableIgnoreParameters(3));
        executor.submit(createRunnableIgnoreParameters(3));
        executor.submit(createRunnableIgnoreParameters(4));
        executor.submit(createRunnableIgnoreParameters(4));

        executor.shutdown();
        assertTrue(executor.awaitTermination(4, TimeUnit.SECONDS));

        Map.Entry<Date, String>[] logMessages = appender.getLogs().stream().filter(s -> s.getValue().matches(CREATE_RUNNABLE_IGNORE_PARAMETERS + "\\d:.*")).toArray((IntFunction<Map.Entry<Date, String>[]>) Map.Entry[]::new);

        assertEquals(10, logMessages.length);
        for (int i = 0; i < 10; i++) {
            assertEquals(CREATE_RUNNABLE_IGNORE_PARAMETERS + ORDER_LIST.get(i) + ":", logMessages[i].getValue());
        }
    }

    @Test
    public void withParametersTest() throws InterruptedException {
        int count = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 2; k++) {
                    executor.submit(createRunnableWithParameters(count++, i, j, k));
                }
            }
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(4, TimeUnit.SECONDS));

        Map.Entry<Date, String>[] logMessages = appender.getLogs().stream().filter(s -> s.getValue().matches(CREATE_RUNNABLE_WITH_PARAMETERS + "\\d:.*")).toArray((IntFunction<Map.Entry<Date, String>[]>) Map.Entry[]::new);

        assertEquals(10, logMessages.length);

        List<Map.Entry<Date, String>> zeroZero = Arrays.stream(logMessages).filter(s -> s.getValue().contains(": 0 -") && s.getValue().endsWith("- 0")).collect(Collectors.toList());
        List<Map.Entry<Date, String>> zeroOne = Arrays.stream(logMessages).filter(s -> s.getValue().contains(": 0 -") && s.getValue().endsWith("- 1")).collect(Collectors.toList());
        List<Map.Entry<Date, String>> oneZero = Arrays.stream(logMessages).filter(s -> s.getValue().contains(": 1 -") && s.getValue().endsWith("- 0")).collect(Collectors.toList());
        List<Map.Entry<Date, String>> oneOne = Arrays.stream(logMessages).filter(s -> s.getValue().contains(": 1 -") && s.getValue().endsWith("- 1")).collect(Collectors.toList());

        assertTrue(zeroZero.get(0).getKey().getTime() + 500 <= zeroZero.get(1).getKey().getTime());
        assertTrue(zeroZero.get(1).getKey().getTime() + 500 <= zeroZero.get(2).getKey().getTime());
        assertTrue(zeroOne.get(1).getKey().getTime() + 500 <= zeroOne.get(2).getKey().getTime());
        assertTrue(zeroOne.get(0).getKey().getTime() + 500 <= zeroOne.get(1).getKey().getTime());
        assertTrue(oneZero.get(0).getKey().getTime() + 500 <= oneZero.get(1).getKey().getTime());
        assertTrue(oneOne.get(0).getKey().getTime() + 500 <= oneOne.get(1).getKey().getTime());
    }

    @Test
    public void recursiveLockTest() throws InterruptedException {
        executor.submit(createRunnableRecursiveLock(1));
        executor.submit(createRunnableRecursiveLock(2));
        executor.submit(createRunnableRecursiveLock(3));
        executor.submit(createRunnableRecursiveLock(4));

        executor.shutdown();
        assertTrue(executor.awaitTermination(6, TimeUnit.SECONDS));

        Map.Entry<Date, String>[] logMessages = appender.getLogs().stream().filter(s -> s.getValue().matches(CREATE_RUNNABLE_RECURSIVE_LOCK + "\\d:.*")).toArray((IntFunction<Map.Entry<Date, String>[]>) Map.Entry[]::new);

        assertEquals(26, logMessages.length);
        int index = 0;
        for (int i = 0; i < 4; i++) {
            switch (ORDER_LIST.get(i)) {
                case 1:
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "1: 1 - 0", logMessages[index++].getValue());
                    break;
                case 2:
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "2: 2 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "2: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "2: 2 - 0", logMessages[index++].getValue());
                    break;
                case 3:
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 3 - 2", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 2 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 2 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 3 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "3: 3 - 0", logMessages[index++].getValue());
                    break;
                case 4:
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 4 - 3", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 3 - 2", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 2 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 2 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 3 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 3 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 4 - 2", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 2 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 2 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 4 - 1", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 1 - 0", logMessages[index++].getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK + "4: 4 - 0", logMessages[index++].getValue());
                    break;
                default:
                    fail("Ungültige Zahl");
            }
        }
    }

    @Test
    public void recursiveLockWithParametersTest() throws InterruptedException {
        executor.submit(createRunnableRecursiveLockWithParameter(1, 1));
        executor.submit(createRunnableRecursiveLockWithParameter(2, 2));
        executor.submit(createRunnableRecursiveLockWithParameter(3, 3));
        executor.submit(createRunnableRecursiveLockWithParameter(4, 4));
        executor.submit(createRunnableRecursiveLockWithParameter(5, 1));
        executor.submit(createRunnableRecursiveLockWithParameter(6, 2));
        executor.submit(createRunnableRecursiveLockWithParameter(7, 3));
        executor.submit(createRunnableRecursiveLockWithParameter(8, 4));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        Map.Entry<Date, String>[] logMessages = appender.getLogs().stream().filter(s -> s.getValue().matches(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "\\d:.*")).toArray((IntFunction<Map.Entry<Date, String>[]>) Map.Entry[]::new);

        for (int i = 1; i < 5; i++) {
            int j = i;
            List<Map.Entry<Date, String>> firstHalf = Arrays.stream(logMessages).filter(s -> s.getValue().contains(j + ":")).collect(Collectors.toList());
            List<Map.Entry<Date, String>> secondHalf = Arrays.stream(logMessages).filter(s -> s.getValue().contains((j + 4) + ":")).collect(Collectors.toList());

            switch (i) {
                case 1:
                    assertEquals(1, firstHalf.size());
                    assertEquals(1, secondHalf.size());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "1: 1 - 0", firstHalf.get(0).getValue());
                    break;
                case 2:
                    assertEquals(3, firstHalf.size());
                    assertEquals(3, secondHalf.size());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "2: 2 - 1", firstHalf.get(0).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "2: 1 - 0", firstHalf.get(1).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "2: 2 - 0", firstHalf.get(2).getValue());
                    break;
                case 3:
                    assertEquals(7, firstHalf.size());
                    assertEquals(7, secondHalf.size());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 3 - 2", firstHalf.get(0).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 2 - 1", firstHalf.get(1).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 1 - 0", firstHalf.get(2).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 2 - 0", firstHalf.get(3).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 3 - 1", firstHalf.get(4).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 1 - 0", firstHalf.get(5).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "3: 3 - 0", firstHalf.get(6).getValue());
                    break;
                case 4:
                    assertEquals(15, firstHalf.size());
                    assertEquals(15, secondHalf.size());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 4 - 3", firstHalf.get(0).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 3 - 2", firstHalf.get(1).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 2 - 1", firstHalf.get(2).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 1 - 0", firstHalf.get(3).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 2 - 0", firstHalf.get(4).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 3 - 1", firstHalf.get(5).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 1 - 0", firstHalf.get(6).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 3 - 0", firstHalf.get(7).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 4 - 2", firstHalf.get(8).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 2 - 1", firstHalf.get(9).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 1 - 0", firstHalf.get(10).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 2 - 0", firstHalf.get(11).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 4 - 1", firstHalf.get(12).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 1 - 0", firstHalf.get(13).getValue());
                    assertEquals(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + "4: 4 - 0", firstHalf.get(14).getValue());
                    break;
            }
            if (ORDER_LIST.indexOf(i) < ORDER_LIST.indexOf(i + 4)) {
                assertTrue(firstHalf.stream().allMatch(e -> secondHalf.stream().allMatch(e2 -> e.getKey().before(e2.getKey()))));
            } else {
                assertTrue(firstHalf.stream().allMatch(e -> secondHalf.stream().allMatch(e2 -> e.getKey().after(e2.getKey()))));
            }
        }
    }

    @Test
    public void multiLockedMethodTest() throws InterruptedException {
        executor.submit(createRunnableMultiLockedMethod1(1));
        executor.submit(createRunnableMultiLockedMethod1(2));
        executor.submit(createRunnableMultiLockedMethod1(3));
        executor.submit(createRunnableMultiLockedMethod1(4));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        Map.Entry<Date, String>[] logMessages = appender.getLogs().stream().filter(s -> s.getValue().matches(CREATE_RUNNABLE_MULTI_LOCKED_METHOD + "\\d")).toArray((IntFunction<Map.Entry<Date, String>[]>) Map.Entry[]::new);

        for (int i = 1; i <= 4; i++) {
            int j = i;
            List<Map.Entry<Date, String>> list = Arrays.stream(logMessages).filter(s -> s.getValue().endsWith(j + "")).collect(Collectors.toList());
            assertEquals(1, list.size());
        }
    }

    private Runnable createRunnableExceptionThrowing(int number) {
        return () -> {
            ThreadContext.put("TID", "" + Thread.currentThread().getId());
            try {
                MethodsAnnotatedWithLock methodsAnnotatedWithLock = new MethodsAnnotatedWithLock();
                wait50ms(number);
                methodsAnnotatedWithLock.exceptionThrowing(CREATE_RUNNABLE_EXCEPTION_THROWING + number, number);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable createRunnableIgnoreParameters(int number) {
        return () -> {
            ThreadContext.put("TID", "" + Thread.currentThread().getId());
            try {
                MethodsAnnotatedWithLock methodsAnnotatedWithLock = new MethodsAnnotatedWithLock();
                wait50ms(number);
                methodsAnnotatedWithLock.ignoreParameters(CREATE_RUNNABLE_IGNORE_PARAMETERS + number);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable createRunnableWithParameters(int nameNumber, int number1, int number2, int number3) {
        return () -> {
            ThreadContext.put("TID", "" + Thread.currentThread().getId());
            try {
                MethodsAnnotatedWithLock methodsAnnotatedWithLock = new MethodsAnnotatedWithLock();
                wait50ms(nameNumber);
                methodsAnnotatedWithLock.ignoreSomeParameters(CREATE_RUNNABLE_WITH_PARAMETERS + nameNumber, number1, number2, number3);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable createRunnableRecursiveLock(int number) {
        return () -> {
            ThreadContext.put("TID", "" + Thread.currentThread().getId());
            try {
                MethodsAnnotatedWithLock methodsAnnotatedWithLock = new MethodsAnnotatedWithLock();
                wait50ms(number);
                methodsAnnotatedWithLock.recursiveLock(CREATE_RUNNABLE_RECURSIVE_LOCK + number, number);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable createRunnableRecursiveLockWithParameter(int nameNumber, int number) {
        return () -> {
            ThreadContext.put("TID", "" + Thread.currentThread().getId());
            try {
                MethodsAnnotatedWithLock methodsAnnotatedWithLock = new MethodsAnnotatedWithLock();
                wait50ms(nameNumber);
                methodsAnnotatedWithLock.recursiveLockWithParameter(CREATE_RUNNABLE_RECURSIVE_LOCK_WITH_PARAMETER + nameNumber, number);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable createRunnableMultiLockedMethod1(int number) {
        return () -> {
            ThreadContext.put("TID", "" + Thread.currentThread().getId());
            try {
                MethodsAnnotatedWithLock methodsAnnotatedWithLock = new MethodsAnnotatedWithLock();
                wait50ms(number);
                methodsAnnotatedWithLock.multiLockedMethod1(CREATE_RUNNABLE_MULTI_LOCKED_METHOD + number);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private void wait50ms(int number) throws InterruptedException {
        synchronized (ORDER_LIST) {
            while (System.currentTimeMillis() - LAST_VISIT < 50) {
                Thread.sleep(10);
            }
            ORDER_LIST.add(number);
            LAST_VISIT = System.currentTimeMillis();
        }
    }
}
