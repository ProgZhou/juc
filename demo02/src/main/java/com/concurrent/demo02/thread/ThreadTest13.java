package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/** 线程活跃性
 * @author ProgZhou
 * @createTime 2022/04/30
 */
@Slf4j
public class ThreadTest13 {
    //测试死锁现象
    @Test
    public void test1(){
        Object lockA = new Object();
        Object lockB = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lockA){
                log.debug("t1 get lockA");
                TimeUtil.sleep(1);
                synchronized (lockB){
                    log.debug("t1 get lockB");
                }
            }
            log.debug("t1 end");
        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB){
                log.debug("t2 get lockB");
                TimeUtil.sleep(1);
                synchronized (lockA){
                    log.debug("t2 get lockA");
                }
            }
            log.debug("t2 end");
        }, "t2");

        t1.start();
        t2.start();
        TimeUtil.sleep(2);
    }

    static volatile int count = 10;
    //活锁
    @Test
    public void test2(){

        new Thread(() -> {
            // 期望减到 0 退出循环
            while (count > 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count--;
                log.debug("count: {}", count);
            }
        }, "t1").start();
        new Thread(() -> {
            // 期望超过 20 退出循环
            while (count < 20) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
                log.debug("count: {}", count);
            }
        }, "t2").start();

    }

    public static void main(String[] args) {
        //哲学家进餐问题
        Chopstick c1 = new Chopstick("c1");
        Chopstick c2 = new Chopstick("c2");
        Chopstick c3 = new Chopstick("c3");
        Chopstick c4 = new Chopstick("c4");
        Chopstick c5 = new Chopstick("c5");

        new Philosopher("Philosopher1", c1, c2).start();
        new Philosopher("Philosopher2", c2, c3).start();
        new Philosopher("Philosopher3", c3, c4).start();
        new Philosopher("Philosopher4", c4, c5).start();
        new Philosopher("Philosopher5", c5, c1).start();
    }
}


//使用ReentrantLock解决死锁问题
class Chopstick extends ReentrantLock {
    String name;

    public Chopstick(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "Chopstick{" +
                "name='" + name + '\'' +
                '}';
    }
}

@Slf4j
class Philosopher extends Thread {

    //分别代表哲学家左边的筷子和哲学家右边的筷子
    Chopstick left;
    Chopstick right;

    public Philosopher(String name, Chopstick left, Chopstick right){
        super(name);
        this.left = left;
        this.right = right;
    }

    private void eating(){
        log.debug("eating...");
        TimeUtil.sleep(new Random().nextInt(3));
    }

    @Override
    public void run() {
        //造成死锁的根源
//        while (true){
//            //获取左手边的筷子
//            synchronized (left) {
//                //获取右手边的筷子
//                synchronized (right){
//                    //吃饭
//                    eating();
//                }
//            }
//        }
        while (true){
            //尝试获得左手的筷子
            if (left.tryLock()) {
                try{
                    if(right.tryLock()){
                        try {
                            eating();
                        } finally {
                            right.unlock();
                        }
                    }
                } finally {
                    left.unlock();  //这里注意了，如果获取右手边的筷子失败，会释放左手拿到的筷子
                }
            }
        }
    }
}

@Slf4j
class PhilosopherAnother extends Thread{
    Chopstick left;
    Chopstick right;

    public PhilosopherAnother(String name, Chopstick left, Chopstick right){
        super(name);
        this.left = left;
        this.right = right;
    }

    private void eating(){
        log.debug("eating...");
        TimeUtil.sleep(1);
    }

    @Override
    public void run() {
        while (true){
            //获取左手边的筷子
            synchronized (right) {
                //获取右手边的筷子
                synchronized (left){
                    //吃饭
                    eating();
                }
            }
        }
    }
}
