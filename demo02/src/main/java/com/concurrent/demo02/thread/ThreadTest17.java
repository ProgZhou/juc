package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/** 无锁并发 --- 乐观锁
 * @author ProgZhou
 * @createTime 2022/05/04
 */
@Slf4j
public class ThreadTest17 {
    public static void main(String[] args) {
        Account account1 = new AccountUnsafe(10000);
        Account.demo(account1);

        Account account2 = new AccountCas(10000);
        Account.demo(account2);
    }

    @Test
    public void test1() {
        AtomicInteger atomicInteger = new AtomicInteger(5);

        //相当于++i
        atomicInteger.incrementAndGet();
        //相当于i++
        atomicInteger.getAndIncrement();
        //相当于i = i + 5
        atomicInteger.addAndGet(5);
        //相当于先返回i，再i = i + 5
        atomicInteger.getAndAdd(5);
        //相当于x = x / 10
        atomicInteger.updateAndGet(x -> x / 10);
    }

    AtomicReference<String> ref = new AtomicReference<>("A");

    //ABA问题
    @Test
    public void test2() {
        log.debug("start...");
        String cur = ref.get();
        other();
        TimeUtil.sleep(2);
        log.debug("isAddress: {}", ref.get() == "A");
        log.debug("change A -> C {}", ref.compareAndSet(cur, "C"));
    }


    AtomicStampedReference<String> reference = new AtomicStampedReference<>("A", 0);
    @Test
    public void test3(){
        log.debug("start...");
        String cur = reference.getReference();
        //获取版本号
        int stamp = reference.getStamp();
        log.debug("version: {}", stamp);
        other2();
        TimeUtil.sleep(2);
        log.debug("main version: {}", stamp);
        log.debug("change A -> B, {}", reference.compareAndSet(reference.getReference(), "C", stamp, stamp + 1));
    }

    public void other(){
        new Thread(() -> {
            log.debug("change A->B {}", ref.compareAndSet(ref.get(), "B"));
        }, "t1").start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            log.debug("change B->A {}", ref.compareAndSet(ref.get(), "A"));
        }, "t2").start();
    }

    public void other2(){
        new Thread(() -> {
            int stamp = reference.getStamp();
            log.debug("t1 version: {}",stamp);
            log.debug("change A -> B, {}", reference.compareAndSet(reference.getReference(), "B", stamp, stamp + 1));
        }, "t1").start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            int stamp = reference.getStamp();
            log.debug("t2 version: {}", stamp);
            log.debug("change B -> A, {}", reference.compareAndSet(reference.getReference(), "A", stamp, stamp + 1));
        }, "t2").start();

    }
}

//一个线程不安全的实现 + synchronized实现
class AccountUnsafe implements Account {

    //存款
    private Integer balance;

    public AccountUnsafe(Integer balance) {
        this.balance = balance;
    }

    @Override
    public synchronized Integer getBalance() {
        return balance;
    }

    @Override
    public synchronized void withdraw(Integer amount) {
        balance -= amount;
    }
}
//使用无锁方式保证线程安全
class AccountCas implements Account{
    //注意不是Integer
    private AtomicInteger balance;

    public AccountCas(Integer number) {
        balance = new AtomicInteger(number);
    }

    @Override
    public Integer getBalance() {
        return balance.get();
    }

    @Override
    public void withdraw(Integer amount) {
        while (true) {
            //获取当前的余额
            int cur = balance.get();
            //尝试减去取钱金额
            int next = cur - amount;
            if (balance.compareAndSet(cur, next)) {
                break;
            }
        }
    }
}

interface Account {
    //获取余额
    Integer getBalance();

    //取款
    void withdraw(Integer amount);

    //测试方法，会在方法被调用的时候启动1000个线程取款，每个线程取款10元，如果初始存款为10000元，那么正确地结果应该是0元
    static void demo(Account account) {
        List<Thread> ts = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            ts.add(new Thread(() -> {
                account.withdraw(10);
            }));
        }
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println(account.getBalance()
                + " cost: " + (end-start)/1000000 + " ms");
    }
}
