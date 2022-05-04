package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

/** 变量线程安全分析
 * @author ProgZhou
 * @createTime 2022/04/24
 */
@Slf4j
public class ThreadTest6 {

//    public static void increment(){
//        int i = 10;
//        i++;
//    }

    @Test
    public void test1(){
        ThreadUnsafe test = new ThreadUnsafe();

        Thread t1 = new Thread(() -> {
            test.method1(200);
        }, "t1");

        Thread t2 = new Thread(() -> {
            test.method1(200);
        }, "t2");

        t1.start();
        t2.start();
    }


    @Test
    public void test2(){
        ThreadSafe test = new ThreadSafe();
        Thread t1 = new Thread(() -> {
            log.debug("t1 begin...");
            test.method1(200);
        }, "t1");

        Thread t2 = new Thread(() -> {
            log.debug("t2 begin...");
            test.method1(200);
        }, "t2");

        log.debug("begin...");
        t1.start();
        t2.start();
    }

}

class ThreadUnsafe{
    ArrayList<Integer> list = new ArrayList<>();

    public void method1(int loop){
        for(int i = 0; i < loop; i++){
            method2();
            method3();
        }
    }

    private void method2(){
        list.add(1);
    }

    private void method3(){
        list.remove(0);
    }
}

class ThreadSafe{
    ArrayList<Integer> list = new ArrayList<>();

    public void method1(int loop){
        for(int i = 0; i < loop; i++){
            method2(list);
            method3(list);
        }
    }

    private void method2(ArrayList<Integer> list){
        list.add(1);
    }

    private void method3(ArrayList<Integer> list){
        list.remove(0);
    }
}
