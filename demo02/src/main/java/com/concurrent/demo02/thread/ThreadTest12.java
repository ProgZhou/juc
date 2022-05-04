package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

/** park / unpark
 * @author ProgZhou
 * @createTime 2022/04/28
 */
@Slf4j
public class ThreadTest12 {
    //先park线程，再unpark线程
    @Test
    public void test1(){
        Thread t1 = new Thread(() -> {
            log.debug("t1 start...");
            TimeUtil.sleep(1);
            log.debug("t1 park...");
            LockSupport.park();
            log.debug("resume...");
        },"t1");
        t1.start();
        TimeUtil.sleep(2);
        log.debug("make t1 unpark...");
        LockSupport.unpark(t1);
    }

    //还能够先unpark线程，再park线程
    @Test
    public void test2(){
        Thread t1 = new Thread(() -> {
            log.debug("t1 start...");
            TimeUtil.sleep(2);
            log.debug("t1 park...");
            LockSupport.park();
            log.debug("resume...");
        },"t1");
        t1.start();
        TimeUtil.sleep(1);
        log.debug("make t1 unpark...");
        LockSupport.unpark(t1);
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("t1 start...");
            TimeUtil.sleep(2);
            log.debug("t1 park...");
            LockSupport.park();
            log.debug("resume...");
        },"t1");
        t1.start();
        TimeUtil.sleep(1);
        log.debug("make t1 unpark...");
        LockSupport.unpark(t1);
    }
}
