package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/** Java内存模型 -- 可见性
 * @author ProgZhou
 * @createTime 2022/05/02
 */
@Slf4j
public class ThreadTest16 {

    volatile static boolean run = true;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            while (run){

            }
            log.debug("t1 complete...");
        }, "t1");
        t1.start();

        TimeUtil.sleep(1);
        log.debug("main stop t1");
        run = false;

    }

}
