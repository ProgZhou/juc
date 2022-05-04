package com.concurrent;


//import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 创建线程
 * @author ProgZhou
 * @createTime 2022/04/22
 */
@Slf4j
public class ThreadTest1 {
    private static final Logger logger = LoggerFactory.getLogger(ThreadTest1.class);

    public static void main(String[] args) {
        Thread t = new Thread(() -> logger.debug("running"));

        t.start();
        logger.debug("running");


    }
}
