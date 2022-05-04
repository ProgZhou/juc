package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** 设计模式之顺序控制
 * @author ProgZhou
 * @createTime 2022/05/02
 */
@Slf4j
public class ThreadTest15 {

    //标记t2线程是否执行
    boolean isPrinted = false;
    //wait / notify版本
    @Test
    public void test1() {
        final Object lock = new Object();

        Thread t1 = new Thread(() -> {
            //如果t1先获得锁，就需要判断t2线程是否执行过
            synchronized (lock) {
                //如果没有执行过的话就需要等待t2线程执行完毕后再执行
                while(!isPrinted){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("t1 print 1");
            }

        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (lock) {
                log.debug("t2 print 2");
                isPrinted = true;
                //如果t2线程后执行，t2执行完毕之后唤醒t1线程
                lock.notify();
            }

        }, "t2");

        t1.start();
        t2.start();

        TimeUtil.sleep(1);
    }

    //场景：有三个线程，t1，t2，t3，交替打印abc，t1 -> a t2 -> b t3 -> c
    @Test
    public void test2(){
        //初始化等待标记，初始标记为1，循环5次
        WaitLock lock = new WaitLock(1, 5);
        Thread t1 = new Thread(() -> {
            lock.run(1, 2, 'a');
        }, "t1");
        Thread t2 = new Thread(() -> {
            lock.run(2, 3, 'b');
        }, "t2");
        Thread t3 = new Thread(() -> {
            lock.run(3, 1, 'c');
        }, "t3");
        t1.start();
        t2.start();
        t3.start();

        TimeUtil.sleep(1);
    }

    @Test
    public void test3(){
        ConditionLock lock = new ConditionLock(5);

        //分别为三个线程分配条件变量
        Condition a = lock.newCondition();
        Condition b = lock.newCondition();
        Condition c = lock.newCondition();

        Thread t1 = new Thread(() -> {
            lock.print('a', a, b);
        }, "t1");
        Thread t2 = new Thread(() -> {
            lock.print('b', b, c);
        }, "t2");
        Thread t3 = new Thread(() -> {
            lock.print('c', c, a);
        }, "t3");
        t1.start();
        t2.start();
        t3.start();

        //由于三个线程在刚要打印的时候都被阻塞，所以需要一个开始的启动
        TimeUtil.sleep(1);
        lock.lock();
        try{
            log.debug("start...");
            a.signal();
        } finally {
            lock.unlock();
        }
        TimeUtil.sleep(2);
    }

    public static void main(String[] args) {
        ConditionLock lock = new ConditionLock(5);

        //分别为三个线程分配条件变量
        Condition a = lock.newCondition();
        Condition b = lock.newCondition();
        Condition c = lock.newCondition();

        Thread t1 = new Thread(() -> {
            lock.print('a', a, b);
        }, "t1");
        Thread t2 = new Thread(() -> {
            lock.print('b', b, c);
        }, "t2");
        Thread t3 = new Thread(() -> {
            lock.print('c', c, a);
        }, "t3");

        //由于三个线程在刚要打印的时候都被阻塞，所以需要一个开始的启动
        TimeUtil.sleep(1);
        lock.lock();
        try{
            log.debug("start...");
            a.signal();
        } finally {
            lock.unlock();
        }
    }

}

/*
* 分析：有三个线程t1，t2，t3
* t1    a    1 -> 2
* t2    b    2 -> 3
* t3    c    3 -> 1
* 参考之前打印1，2的方法，不难想到需要一个变量去标记之前的线程是否执行完毕
* 但boolean变量只有两个状态，而这里有三个线程，所以可以使用一个整型变量，去规定
* 每个线程执行完成之后的状态，现在假设t1执行时，状态为1，t2为2，t3为3
* 然后从当前状态推导出下一个状态，比如t1执行完毕后，t2执行，那么1状态之后的状态就是2
*
* 运用面向对象的思想，将这个标记做一个封装
* */
@Slf4j
class WaitLock {
    //等待标记
    private int flag;
    //循环次数
    private int loopNumber;

    public WaitLock() {
    }

    public WaitLock(int flag, int loopNumber) {
        this.flag = flag;
        this.loopNumber = loopNumber;
    }

    /**
     * 线程运行
     * @param status 当前线程的等待标记
     * @param nextStatus 下一个线程的等待标记
     * @param ch 线程要打印的字符
     */
    public void run(int status, int nextStatus, char ch){
        for (int i = 0; i < loopNumber; i++) {
            synchronized (this) {
                while(status != flag){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("{}", ch);
                //更新标记
                flag = nextStatus;
                //唤醒其他等待的线程
                this.notifyAll();
            }
        }
    }
}

/**
 * 对上面的代码进行改进，使用ReentrantLock来进行
 * 分析：之前使用一个整型变量来标记每个线程的等待状态，当把锁换成ReentrantLock之后
 * 一个ReentrantLock有多个条件变量，所以可以给这三个线程分别分配一个条件变量，比如
 * Thread     Condition
 * t1          a
 * t2          b
 * t3          c
 * 通过唤醒的方式来控制打印的顺序
 */

@Slf4j
class ConditionLock extends ReentrantLock {
    //循环次数
    private int loopNumber;

    public ConditionLock() {
    }

    public ConditionLock(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    /**
     * 打印方法
     * @param ch 线程要打印的字符
     * @param current 当前线程的条件变量
     * @param next 下一个要被唤醒线程的条件变量
     */
    public void print(char ch, Condition current, Condition next){
        for (int i = 0; i < loopNumber; i++) {
            //先获得锁
            this.lock();
            try {
                try {
                    //在打印之前先让线程进入各自的waitSet中等待
                    current.await();
                    log.debug("{}", ch);
                    //唤醒下一个要打印的线程
                    next.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                this.unlock();
            }
        }
    }
}
