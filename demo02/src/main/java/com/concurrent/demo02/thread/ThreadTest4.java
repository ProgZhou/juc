package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author ProgZhou
 * @createTime 2022/04/23
 */
@Slf4j
public class ThreadTest4 {

    //守护线程
    @Test
    public void test1(){
        Thread t1 = new Thread(() -> {
            log.debug("t1 begin...");
            while (true){
                Thread currentThread = Thread.currentThread();
                if(currentThread.isInterrupted()){
                    log.debug("interrupted");
                    break;
                }
            }
            log.debug("t1 end...");
        }, "t1");

        //设置t1为守护线程
        t1.setDaemon(true);
        t1.start();

        TimeUtil.sleep(2);
        log.debug("main thread end...");
    }


    //线程的六种状态
    @Test
    public void test2(){
        Thread t1 = new Thread(() -> {
            log.debug("t1 running...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            log.debug("t2 running...");
            while (true){
                Thread currentThread = Thread.currentThread();
                if(currentThread.isInterrupted()){
                    log.debug("interrupt");
                    break;
                }
            }
        }, "t2");
        t2.start();

        Thread t3 = new Thread(() -> {
            log.debug("t3 running...");
        }, "t3");
        t3.start();

        Thread t4 = new Thread(() -> {
            synchronized (ThreadTest4.class){
                TimeUtil.sleep(100);
            }
        }, "t4");
        t4.start();

        Thread t5 = new Thread(() -> {
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");
        t5.start();

        Thread t6 = new Thread(() -> {
            synchronized (ThreadTest4.class){
                log.debug("t6 running...");
            }
        }, "t6");
        t6.start();

        log.debug("t1 state: {}", t1.getState());
        log.debug("t2 state: {}", t2.getState());
        log.debug("t3 state: {}", t3.getState());
        log.debug("t4 state: {}", t4.getState());
        log.debug("t5 state: {}", t5.getState());
        log.debug("t6 state: {}", t6.getState());


    }

}
