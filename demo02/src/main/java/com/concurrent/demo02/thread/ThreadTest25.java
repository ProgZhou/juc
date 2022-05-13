package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** ReadWirteLock --- 读写锁
 * @author ProgZhou
 * @createTime 2022/05/13
 */
@Slf4j
public class ThreadTest25 {
    @Test
    public void test1() {
        DataContainer data = new DataContainer();

        new Thread(data::write, "t1").start();

        new Thread(data::read, "t2").start();

        TimeUtil.sleep(2);
    }

    @Test
    public void test2() {
        //GenericDao dao = new GenericDao();
        GenericCachedDao dao = new GenericCachedDao();
        log.debug("select...");
        String sql = "select * from emp where empno = ?";
        int empno = 3306;
        String result = dao.queryOne(String.class, sql, empno);
        log.debug("result : {}", result);
        result = dao.queryOne(String.class, sql,  empno);
        log.debug("result : {}", result);
        result = dao.queryOne(String.class, sql,  empno);
        log.debug("result : {}", result);

        log.debug("update...");
        dao.update("update emp set sal = ? where empon = ?", 1000, empno);
        result = dao.queryOne(String.class, sql,  empno);
        log.debug("result : {}",result);

    }
}

//数据容器
@Slf4j
class DataContainer {

    //可以是int，也可以是其他类型，Object等
    private int data;
    //读写锁
    private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    //读锁
    private ReentrantReadWriteLock.ReadLock r = rw.readLock();
    //写锁
    private ReentrantReadWriteLock.WriteLock w = rw.writeLock();

    //读操作
    public void read() {
        //用读锁保护读操作
        r.lock();
        try {
            log.debug("read...");
            TimeUtil.sleep(1);
        } finally {
            log.debug("release read lock");
            r.unlock();
        }
    }


    //写操作
    public void write() {
        //用写锁保护写操作
        w.lock();
        try {
            log.debug("write...");
        } finally {
            log.debug("release write lock");
            w.unlock();
        }
    }
}

//模拟查询数据库的通用类
@Slf4j
class GenericDao {

    private String result = "1";

    //从数据库中查询一条数据
    public <T> T queryOne(Class<T> beanClass, String sql, Object ...args) {
        log.debug("sql: [{}] params: {}", sql, Arrays.toString(args));
        Object success = new Object();
        return (T) result;
    }

    //更新操作
    public int update(String sql, Object ...args) {
        log.debug("sql: [{}] params: {}", sql, Arrays.toString(args));
        result = "2";
        return 1;
    }
}

@Slf4j
class GenericCachedDao extends GenericDao {

    private GenericDao dao = new GenericDao();

    private Map<SqlPair, Object> cache = new HashMap<>();

    //使用读写锁来解决多线程查询的问题
    private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();

    //封装sql语句以及参数作为缓存的键
    private class SqlPair {
        private String sql;
        private Object[] args;

        public SqlPair(String sql, Object[] args) {
            this.sql = sql;
            this.args = args;
        }

        //重写hashCode与equals方法用于对象比较


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SqlPair sqlPair = (SqlPair) o;
            return Objects.equals(sql, sqlPair.sql) && Arrays.equals(args, sqlPair.args);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(sql);
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }
    }

    //重写父类中的方法，使其带有缓存

    @Override
    public <T> T queryOne(Class<T> beanClass, String sql, Object... args) {
        //先从缓存中查找
        SqlPair key = new SqlPair(sql, args);
        //读取缓存时加上读锁
        rw.readLock().lock();
        try {
            T o = (T) cache.get(key);
            if(o != null) {
                return o;
            }
        } finally {
            //在获得写锁之前一定要释放读锁，读写锁不支持锁升级的重入
            rw.readLock().unlock();
        }
        //在更新缓存的时候使用写锁
        rw.writeLock().lock();
        try {
            //如果有多个线程同时执行到了这里，虽然只有一个线程能够执行代码块中的代码，但释放写锁之后，如果不加以判断，多个线程还是会执行多次查询操作
            //所以在这里需要再一次查询缓存
            T value = (T) cache.get(key);
            if(value != null) {
                return value;
            }
            //如果没找到再调用父类的方法进行查询，并放入缓存
            value = dao.queryOne(beanClass, sql, args);
            cache.put(key, value);
            return value;
        } finally {
            rw.writeLock().unlock();
        }
    }

    @Override
    public int update(String sql, Object... args) {
        //同理，在更新的时候使用写锁保护
        rw.writeLock().lock();
        try {
            //先更新数据，再清空缓存
            int update = dao.update(sql, args);
            //清空缓存
            cache.clear();
            return update;
        } finally {
            rw.writeLock().unlock();
        }
    }
}

