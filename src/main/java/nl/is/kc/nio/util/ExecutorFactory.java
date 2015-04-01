package nl.is.kc.nio.util;

import java.util.concurrent.*;

/**
 * Created by rubenernst on 01-04-15.
 */
public class ExecutorFactory {
    public static ExecutorService createExecutor() {
        return createExecutor(5, 200);
    }

    public static ExecutorService createExecutor(int corePoolSize, int maximumPoolSize) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue(1000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
