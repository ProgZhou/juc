package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/** AQS原理
 * @author ProgZhou
 * @createTime 2022/05/10
 */
@Slf4j
public class ThreadTest24 {
}

//自定义锁，实现Lock接口（不可重入）
class MyLock implements Lock{

    private MySync sync = new MySync();

    //自定义同步器类
    class MySync extends AbstractQueuedSynchronizer {
        //尝试获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(0, 1)) {
                //如果加上了锁，并设置锁的持有者为当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        //尝试释放锁
        @Override
        protected boolean tryRelease(int arg) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        //是否持有独占锁
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        public Condition newCondition() {
            return new ConditionObject();
        }
    }

    //上锁方法，不成功则进入等待队列
    @Override
    public void lock() {
        sync.acquire(1);
    }

    //可打断上锁
    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    //尝试加锁（尝试一次）
    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    //带超时的尝试加锁
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    //解锁
    @Override
    public void unlock() {
        sync.release(1);
    }

    //创建条件变量
    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}
