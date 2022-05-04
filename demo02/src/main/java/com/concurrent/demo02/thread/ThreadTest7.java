package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/** 卖票的线程安全问题
 * @author ProgZhou
 * @createTime 2022/04/25
 */
@Slf4j
public class ThreadTest7 {

    private static Random random = new Random();

    //产生1-5的随机数
    public static int randomCount(){
        return random.nextInt(5) + 1;
    }

    //卖票问题
    @Test
    public void test1(){
        TicketWindow ticketWindow = new TicketWindow(2000);


        List<Thread> threadList = new ArrayList<>();

        List<Integer> sellCount = new Vector<>();
        //创建多个线程模拟多个抢票
        for (int i = 0; i < 200; i++) {
            Thread thread = new Thread(() -> {

                try {
                    Thread.sleep(randomCount() * 10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //模拟一个人买票，随机生成这个人想买的票数
                int sell = ticketWindow.sell(randomCount());
                //由于在线程内，需要保证add操作的线程安全，使用Vector
                sellCount.add(sell);
            });

            //因为线程还未启动，所以可以使用ArrayList
            threadList.add(thread);

            thread.start();
        }

        //主线程需要等待上述线程执行完毕
        threadList.forEach((t) -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        int sum = sellCount.stream().mapToInt(c -> c).sum();
        //如果是线程安全的，那么被购买的票数与剩余的票数之和应为初始化时的票数
        log.debug("sell count: {}", sum);

        log.debug("remainder count: {}", ticketWindow.getCount());
    }



}

class TicketWindow{
    //总票数
    private int count;


    public TicketWindow(int count) {
        this.count = count;
    }

    //获取余票
    public int getCount() {
        return count;
    }

    //返回每个人买的票数
    public int sell(int amount) {
        synchronized (this) {
            if (this.count >= amount) {
                this.count -= amount;
                return amount;
            } else {
                return 0;
            }
        }
    }

}
