package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/** wait / notify方法
 * @author ProgZhou
 * @createTime 2022/04/27
 */
@Slf4j
public class ThreadTest9 {

    private final Object lock = new Object();

    @Test
    public void test1(){
        Thread t1 = new Thread(() -> {
            synchronized (lock){
                log.debug("thread t1 running...");

                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("thread t1 continue to running...");
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            log.debug("thread t2 running...");

            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("thread t2 continue to running...");
        }, "t2");

        t1.start();
        t2.start();
        TimeUtil.sleep(2);
        log.debug("wake up other threads...");

        synchronized (lock){
            //唤醒一个线程
//            lock.notify();
            //唤醒多个线程
            lock.notifyAll();
        }
    }

    @Test
    public void test2(){
        Thread t1 = new Thread(() -> {
            synchronized (lock){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1");
        t1.start();

        TimeUtil.sleep(1);
        log.debug("t1 state: {}", t1.getState());
        synchronized (lock){
            log.debug("lock...");
        }
    }

}
