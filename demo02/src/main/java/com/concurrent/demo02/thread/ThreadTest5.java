package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/** 线程安全问题
 * @author ProgZhou
 * @createTime 2022/04/23
 */
@Slf4j
public class ThreadTest5 {

    private static int count = 0;

    //线程安全问题的体现
    @Test
    public void test1(){

        //线程1对count变量做5000次++运算
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                count++;
            }
        }, "t1");

        //线程2对count变量做5000次--运算
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                count--;
            }
        }, "t2");

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.debug("count = {}", count);
    }

    //使用synchronized关键字解决临界区问题
    @Test
    public void test2(){
        //线程1对count变量做5000次++运算
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (ThreadTest5.class){
                    count++;
                }
            }
        }, "t1");

        //线程2对count变量做5000次--运算
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (ThreadTest5.class){
                    count--;
                }
            }
        }, "t2");

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.debug("count = {}", count);
    }


    //面向对象synchronized优化
    @Test
    public void test3(){
        LockObject lockObject = new LockObject();
        //线程1对count变量做5000次++运算
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                lockObject.increment();
            }
        }, "t1");

//线程2对count变量做5000次--运算
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                lockObject.decrement();
            }
        }, "t2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        log.debug("count = {}", lockObject.getCount());
    }
}


class LockObject{
    private int count = 0;

    //自增方法
    public void increment(){
        synchronized(LockObject.class){
            count++;
        }
    }

    //自减方法
    public void decrement(){
        synchronized(LockObject.class){
            count--;
        }
    }

    public int getCount(){
        return count;
    }
}