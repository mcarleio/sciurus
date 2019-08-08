package io.mcarle.sciurus.retry;

import io.mcarle.sciurus.annotation.Retry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MethodsAnnotatedWithRetry {

    private static final Logger LOG = LogManager.getLogger(MethodsAnnotatedWithRetry.class);

    protected int run = 0;

    @Retry
    public void successfullMethodWithOneRetry() {
        LOG.info("successfullMethodWithOneRetry");
    }

    @Retry(times = 0)
    public void successfullMethodWithZeroRetry() {
        LOG.info("successfullMethodWithZeroRetry");
    }

    @Retry(times = -5)
    public void successfullMethodWithMinusFiveRetry() {
        LOG.info("successfullMethodWithMinusFiveRetry");
    }

    @Retry(times = 5)
    public void successfullMethodWithFiveRetry() {
        LOG.info("successfullMethodWithFiveRetry");
    }

    @Retry
    public void exceptionThrowingWithOneRetry() {
        LOG.info("exceptionThrowingWithOneRetry");
        throw new RuntimeException();
    }

    @Retry(times = 0)
    public void exceptionThrowingWithZeroRetries() {
        LOG.info("exceptionThrowingWithZeroRetries");
        throw new RuntimeException();
    }

    @Retry(times = -5)
    public void exceptionThrowingWithMinusFiveRetries() {
        LOG.info("exceptionThrowingWithMinusFiveRetries");
        throw new RuntimeException();
    }

    @Retry(times = 5)
    public void exceptionThrowingWithFiveRetry() {
        LOG.info("exceptionThrowingWithFiveRetry");
        throw new RuntimeException();
    }

    @Retry(times = 2)
    public void successAfterXTriesWithTwoRetries(int x) {
        LOG.info("successAfterXTriesWithTwoRetries");
        run++;
        if (run < x) {
            throw new RuntimeException();
        }
    }

    @Retry(times = 2)
    public int successAfterXTriesWithTwoRetriesReturnResult(int x) {
        LOG.info("successAfterXTriesWithTwoRetriesReturnResult");
        run++;
        if (run < x) {
            throw new RuntimeException();
        }
        return run;
    }

}

