package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;

/** 线程创建方法
 * @author ProgZhou
 * @createTime 2022/04/22
 */
@Slf4j
public class ThreadTest1 {
    public static void main(String[] args) {
        //方式一，继承Thread类，重写run方法
//        Thread t = new Thread(() -> log.debug("running"));
//
//        t.start();
//        log.debug("running");

        //方式二，实现Runnable接口，实现run方法
        Runnable r = () -> {
            log.debug("running");
        };

        Thread t = new Thread(r, "runnable");
        t.start();
    }
}
