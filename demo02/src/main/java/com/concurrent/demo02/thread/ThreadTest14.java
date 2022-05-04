package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** ReentrantLock测试
 * @author ProgZhou
 * @createTime 2022/04/30
 */
@Slf4j
public class ThreadTest14 {
    //创建一个ReentrantLock对象
    private static ReentrantLock lock = new ReentrantLock();

    public void method1(){
        lock.lock();

        try {
            log.debug("method1...");
            method2();
        } finally {
            lock.unlock();
        }
    }

    public void method2(){
        lock.lock();

        try {
            log.debug("method2...");
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void test1(){
        lock.lock();
        try {
            log.debug("main...");
            method1();
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void test2(){
        Thread t1 = new Thread(() -> {
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.debug("get lock failed, return");
                return;
            }
            try {
                log.debug("get lock successfully");
            } finally {
                lock.unlock();
            }
        }, "t1");


        lock.lock();
        t1.start();
        TimeUtil.sleep(2);
        //打断t1，防止t1永久等待
        t1.interrupt();
    }

    public void test3(){
        Thread t1 = new Thread(() -> {
            log.debug("t1 try to get lock...");
            //如果不带任何参数，就是判断一次，如果这次没获取锁就直接返回
            if (! lock.tryLock()) {
                log.debug("t1 get lock failed, return");
                return;
            }
            try {
                log.debug("get lock successfully");
            } finally {
                lock.unlock();
            }
        }, "t1");

        t1.start();
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("t1 try to get lock...");
            //如果不带任何参数，就是判断一次，如果这次没获取锁就直接返回
//            if (! lock.tryLock()) {
//                log.debug("t1 get lock failed, return");
//                return;
//            }
            try {
                if (! lock.tryLock(2, TimeUnit.SECONDS)) {
                    log.debug("t1 get lock failed, return");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.debug("t1 is interrupted...");
                return;
            }
            try {
                log.debug("get lock successfully");
            } finally {
                lock.unlock();
            }
        }, "t1");

        log.debug("main get lock");
        lock.lock();
        t1.start();
        TimeUtil.sleep(1);
        log.debug("main release lock");
        lock.unlock();

    }
}
