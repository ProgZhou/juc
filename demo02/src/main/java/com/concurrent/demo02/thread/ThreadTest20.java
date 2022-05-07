package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/** JDK提供的线程池
 * @author ProgZhou
 * @createTime 2022/05/07
 */
@Slf4j
public class ThreadTest20 {
    //newFixedThreadPool
    @Test
    public void test1() {
        ExecutorService pool = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "pool_t" + threadNumber.getAndIncrement());
            }
        });

        pool.execute(() -> {
            log.debug("1");
        });
        pool.execute(() -> {
            log.debug("2");
        });
        pool.execute(() -> {
            log.debug("3");
        });
        pool.execute(() -> {
            log.debug("4");
        });

        TimeUtil.sleep(1);

    }

    @Test
    public void test2() {
        Executors.newCachedThreadPool(new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "pool_t" + threadNumber.getAndIncrement());
            }
        });
    }

    @Test
    public void test3() {
        ExecutorService pool = Executors.newSingleThreadExecutor();

        pool.execute(() -> {
            log.debug("1");
            int i = 1 / 0;
        });
        pool.execute(() -> {
            log.debug("2");
        });
        pool.execute(() -> {
            log.debug("3");
        });
        pool.execute(() -> {
            log.debug("4");
        });
        TimeUtil.sleep(1);
    }

    @Test
    public void test4() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String> result = pool.submit(() -> {
            log.debug("begin...");
            log.debug("1");
            log.debug("end");
            return "1";
        });

        TimeUtil.sleep(1);
        try {
            log.debug("{}", result.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<String>> futureList = pool.invokeAll(Arrays.asList(
                () -> {
                    log.debug("begin t1");
                    sleep(1000);
                    log.debug("t1 end");
                    return "1";
                }, () -> {
                    log.debug("begin t2");
                    sleep(500);
                    log.debug("t2 end");
                    return "2";
                }, () -> {
                    log.debug("begin t3");
                    sleep(2000);
                    log.debug("t3 end");
                    return "3";
                }
        ));
        futureList.forEach(f -> {
            try {
                log.debug("{}", f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    @Test
    public void test6() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        String s = pool.invokeAny(Arrays.asList(
                () -> {
                    log.debug("begin t1");
                    sleep(1000);
                    log.debug("t1 end");
                    return "1";
                }, () -> {
                    log.debug("begin t2");
                    sleep(500);
                    log.debug("t2 end");
                    return "2";
                }, () -> {
                    log.debug("begin t3");
                    sleep(2000);
                    log.debug("t3 end");
                    return "3";
                }
        ));
        log.debug("{}", s);
    }
}
