package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/** 线程常用方法1
 * @author ProgZhou
 * @createTime 2022/04/22
 */
@Slf4j
public class ThreadTest3 {
    @Test
    public void test1(){
        Thread thread = new Thread(
                () -> log.debug("running")
        );

        //从结果中可以看出是由主线程打印的running，所以这个run方法仍然是由主线程执行到此处使用thread实例对象调用的
        // thread.run();
        log.debug("Thread state: {}", thread.getState());
        thread.start();
        log.debug("Thread state: {}", thread.getState());


    }

    /**
     * sleep与yield
     */
    @Test
    public void test2(){
        Thread thread = new Thread(
                () -> {
                    log.debug("running");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        thread.start();
        log.debug("thread state: {}", thread.getState());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread.interrupt();

//        log.debug("thread state: {}", thread.getState());
    }

    private static int r = 0;
    @Test
    public void test3(){
        log.debug("main begin...");

        Thread t = new Thread(() -> {
            log.debug("t1 begin...");
            //线程睡眠1s
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            r = 10;

            log.debug("t1 end...");
        }, "t1");

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("r = {}", r);
        log.debug("main end...");
    }

    private static int r1 = 0;
    private static int r2 = 0;

    //主线程等待多个线程的结果
    @Test
    public void test4() throws InterruptedException {

        Thread t1 = new Thread(() -> {
            log.debug("t1 begin...");
            TimeUtil.sleep(1);
            r1 = 10;
            log.debug("t1 end...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            log.debug("t2 begin...");
            TimeUtil.sleep(2);
            r2 = 20;
            log.debug("t2 end...");
        }, "t2");

        t1.start();
        t2.start();
        long start = System.currentTimeMillis();
        log.debug("join...");
        t1.join();
        t2.join();
        log.debug("join end...");
        long end = System.currentTimeMillis();
        log.debug("r1: {}, r2: {}, costs: {}", r1, r2, end - start);

    }


    //interrupt方法
    @Test
    public void test5(){
        Thread t1 = new Thread(() -> {
            TimeUtil.sleep(3);
        }, "t1");

        Thread t2 = new Thread(() ->{
            while (true){
                boolean interrupted = Thread.currentThread().isInterrupted();
                if(interrupted){
                    log.debug("Thread is interrupted");
                    break;
                }
            }
        }, "t2");

//        t1.start();
//        TimeUtil.sleep(1);
//        //打断正在sleep的线程
//        t1.interrupt();
//        log.debug("t1 interrupt");
//        log.debug("isInterrupted: {}", t1.isInterrupted());
        t2.start();
        TimeUtil.sleep(1);
        t2.interrupt();
        log.debug("interrupt: {}", t2.isInterrupted());
    }


    //两阶段打断
    @Test
    public void test6(){
        TwoPhaseTermination test = new TwoPhaseTermination();

        test.start();
        //5s后终止监控程序
        TimeUtil.sleep(5);
        test.stop();
    }


}
@Slf4j
class TwoPhaseTermination{

    //模拟监控线程
    private Thread monitor;

    //启动监控线程
    public void start(){
        monitor = new Thread(() -> {
            while(true){
                Thread currentThread = Thread.currentThread();
                if(currentThread.isInterrupted()){
                    log.debug("处理后事...");
                    break;
                }

                //每隔1秒执行一次监控程序
                try {
                    Thread.sleep(2000);
                    log.debug("执行监控程序...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //如果在睡眠的时候被打断，那么就不会将打断标志置为true，所以需要在这里重新设置打断标志
                    currentThread.interrupt();
                }

            }
        }, "monitor");

        monitor.start();
    }

    public void stop(){
        monitor.interrupt();
    }
}

class Singleton {
    private static volatile Singleton instance = null;

    private Singleton() {
    }

    public static Singleton getInstance(){
        if(instance == null) {
            synchronized (Singleton.class) {
                if(instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}