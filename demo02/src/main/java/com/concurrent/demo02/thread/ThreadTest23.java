package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/** Fork / join线程池
 * @author ProgZhou
 * @createTime 2022/05/10
 */
@Slf4j
public class ThreadTest23 {
    public static void main(String[] args) {
        //创建线程池，默认CPU核心数大小的线程池
        ForkJoinPool pool = new ForkJoinPool();
//        Integer invoke = pool.invoke(new MyTask(5));
//        log.debug("result: {}", invoke);
        Integer invoke = pool.invoke(new MyTask(1, 5));
        log.debug("result: {}", invoke);

    }
}

//任务：求和 1 - n之间整数的和
@Slf4j
class MyTask extends RecursiveTask<Integer> {

//    private int n;
//
//    public MyTask(int n) {
//        this.n = n;
//    }
    int begin;
    int end;

    public MyTask(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        //分组方式一
//        // 如果 n 已经为 1，可以求得结果了
//        if (n == 1) {
//            log.debug("join() {}", n);
//            return n;
//        }
//
//        // 将任务进行拆分(fork)
//        MyTask t1 = new MyTask(n - 1);
//        t1.fork();
//        log.debug("fork() {} + {}", n, t1);
//
//        // 合并(join)结果
//        int result = n + t1.join();
//        log.debug("join() {} + {} = {}", n, t1, result);
//        return result;

        // 5, 5
        if (begin == end) {
            log.debug("join() {}", begin);
            return begin;
        }

        if (end - begin == 1) {
            log.debug("join() {} + {} = {}", begin, end, end + begin);
            return end + begin;
        }

        // 1 5
        int mid = (end + begin) / 2; // 3
        MyTask t1 = new MyTask(begin, mid); // 1,3
        t1.fork();
        MyTask t2 = new MyTask(mid + 1, end); // 4,5
        t2.fork();
        log.debug("fork() {} + {} = ?", t1, t2);
        int result = t1.join() + t2.join();
        log.debug("join() {} + {} = {}", t1, t2, result);
        return result;
    }
}
