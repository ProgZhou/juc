package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Semaphore;

/** Semaphore --- 信号量
 * @author ProgZhou
 * @createTime 2022/05/13
 */
@Slf4j
public class ThreadTest26 {
    @Test
    public void test1() {
        //设置共享资源的数量为3个
        Semaphore semaphore = new Semaphore(3);
        for(int i = 0; i < 5; i++) {
            new Thread(() -> {
                //获取共享资源
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    log.debug("running...");
                    TimeUtil.sleep(1);
                    log.debug("end");
                } finally {
                    //释放资源
                    semaphore.release();
                }

            }, "t" + (i + 1)).start();
        }
        TimeUtil.sleep(10);
    }
}
