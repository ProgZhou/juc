package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/** 线程创建方法二
 * @author ProgZhou
 * @createTime 2022/04/22
 */
@Slf4j
public class ThreadTest2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            log.debug("running");
            return 100;
        });

        //借助thread来执行
        Thread t = new Thread(futureTask, "futureTask");
        t.start();
        //当主线程执行到这里时，会等待futureTask执行完毕返回结果
        int result = futureTask.get();
        log.debug("get:{}", result);
    }
}
