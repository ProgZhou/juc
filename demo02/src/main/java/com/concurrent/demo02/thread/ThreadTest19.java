package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** 并发工具1 -- 自定义线程池
 * @author ProgZhou
 * @createTime 2022/05/06
 */
@Slf4j
public class ThreadTest19 {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(2, 1000, TimeUnit.MILLISECONDS, 1, (queue, task) -> {
            //其中一种拒绝策略，直接放弃
            log.debug("give up task: {}", task);
        });
        for (int i = 0; i < 5; i++) {
            int j = i;
            threadPool.execute(() -> {
                TimeUtil.sleep(1);
                log.debug("{}", j);
            });
        }
    }
}

//任务队列，当线程池中创建的线程已经达到所规定的最大线程数时，之后进来的任务就会被阻塞住，放到任务队列中等待分配线程
@Slf4j
class BlockingQueue<T> {
    //1. 任务队列，存放待执行的任务
    private Deque<T> blockQueue = new ArrayDeque<>();  //ArrayDeque，一种双向链表的实现，性能比较好
    //2. 锁，保证获取线程的安全性
    private ReentrantLock lock = new ReentrantLock();
    //3. 队列满时的条件变量
    private Condition full = lock.newCondition();
    //4. 队列空时的条件变量
    private Condition empty = lock.newCondition();
    //5. 队列容量
    private int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    //获取队列容量
    public int size() {
        lock.lock();
        try {
            return blockQueue.size();
        } finally {
            lock.unlock();
        }
    }

    //阻塞获取
    public T poll() {
        //加锁
        lock.lock();

        try {
            //如果队列为空，则循环等待
            while(blockQueue.isEmpty()) {
                try {
                    empty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            T t = blockQueue.removeFirst();
            full.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带超时的阻塞获取
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return
     */
    public T poll(long timeout, TimeUnit unit) {
        //加锁
        lock.lock();
        //将时间转换为纳秒
        long nanos = unit.toNanos(timeout);
        try {
            //如果队列为空，则循环等待

            while(blockQueue.isEmpty()) {
                try {
                    if(nanos <= 0) {
                        return null;
                    }
                    //awaitNanos会返回此次等待之后的剩余等待时间
                    nanos = empty.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            T t = blockQueue.removeFirst();
            full.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    //阻塞添加
    public void put(T element) {
        lock.lock();
        try {
            //如果队列满，就不能向其中继续放元素
            while(blockQueue.size() == capacity) {
                try {
                    log.debug("waiting to add...");
                    full.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("taskQueue add task: {}", element);
            blockQueue.addLast(element);
            empty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param element 待添加的任务对象
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 如果添加成功，则返回true，否则返回false
     */
    public boolean put(T element, long timeout, TimeUnit unit) {
        lock.lock();
        try {
            //如果队列满，就不能向其中继续放元素
            long nanos = unit.toNanos(timeout);
            while(blockQueue.size() == capacity) {
                try {
                    log.debug("waiting to add...");
                    if(nanos <= 0) {
                        return false;
                    }
                    nanos = full.awaitNanos(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("taskQueue add task: {}", element);
            blockQueue.addLast(element);
            empty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    //由任务队列去执行这个拒绝策略
    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();

        try {
            //判断队列是否已满
            if(blockQueue.size() == capacity) {
                //执行拒绝策略
                rejectPolicy.reject(this, task);
            } else {
                //如果有空闲的话
                log.debug("taskQueue add task: {}", task);
                blockQueue.addLast(task);
                empty.signal();
            }
        } finally {
            lock.unlock();
        }
    }

}

//拒绝策略，由设计者具体去实现
@FunctionalInterface
interface RejectPolicy<T> {
    void reject(BlockingQueue<T> taskQueue, T task);
}

//线程池
@Slf4j
class ThreadPool {
    //1. 线程池所关联的任务队列
    private BlockingQueue<Runnable> taskQueue;
    //2. 线程池包含的线程集合
    private Set<Workers> threadPool = new HashSet<>();
    //3. 核心线程数
    private int coreSize;
    //4. 线程连接的超时时间
    private int timeout;
    private TimeUnit unit;
    //5. 拒绝策略，由线程池去决定在线程池和任务队列全满的情况下任务的去向
    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int coreSize, int timeout, TimeUnit unit, int capacity) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.unit = unit;
        this.taskQueue = new BlockingQueue<>(capacity);
    }

    public ThreadPool(int coreSize, int timeout, TimeUnit unit, int capacity, RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.unit = unit;
        this.taskQueue = new BlockingQueue<>(capacity);
        this.rejectPolicy = rejectPolicy;
    }

    //线程封装
    class Workers extends Thread{
        private Runnable task;

        public Workers(Runnable task) {
            this.task = task;
        }

        //执行任务
        @Override
        public void run() {
            //当创建线程时就有任务传入，则直接执行
            //如果当前任务执行完毕，就去查看任务队列中是否有被阻塞的任务，如果有则获取任务并执行
            while(task != null || (task = taskQueue.poll(timeout, unit)) != null) {
                try {
                    log.debug("running {}", task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //task执行完毕，置为null
                    task = null;
                }
            }

            //跳出循环就说明当前线程任务已经执行完毕，是一个空闲的线程
            synchronized (threadPool) {
                log.debug("remove thread: {}", this);
                threadPool.remove(this);
            }
        }
    }

    //执行任务
    public void execute(Runnable task) {
        synchronized (threadPool) {
            //如果线程池的大小小于核心线程数，则可以直接将这个任务交给线程去执行
            if(threadPool.size() < coreSize) {
                Workers workers = new Workers(task);
                log.debug("pool add thread: {}, task: {}", workers, task);
                threadPool.add(workers);
                workers.start();
            } else{
                //否则需要进入任务阻塞队列，等待其他线程执行完毕，空闲下来之后再被执行
                //taskQueue.put(task);
                /*
                * 如果任务队列也满了，那么这个put方法的执行或许就直接阻塞住了，一般会有如下的策略处理多出来的任务
                * 1). 死等
                * 2). 带超时的等待
                * 3). 让调用者放弃执行任务
                * 4). 让调用者抛出异常
                * 5). 让调用者自己执行任务
                * 6) ....
                * 可以有很多的策略，如果在这里写死了，那么代码的扩展性就很差，所以，可以把这里的方式抽象为接口，由调用者自己实现
                * */
                //在这里使用这个拒绝策略，由任务队列去执行这个拒绝策略
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }
}

