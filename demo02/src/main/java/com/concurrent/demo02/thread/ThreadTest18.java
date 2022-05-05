package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicIntegerArray;

/** 不可变类
 * @author ProgZhou
 * @createTime 2022/05/05
 */
@Slf4j
public class ThreadTest18 {

    @Test
    public void test1() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    log.debug("{}", sdf.parse("2022-05-05"));
                } catch (Exception e) {
                    log.error("{}", e);
                }
            }).start();
        }
        TimeUtil.sleep(1);
    }

    @Test
    public void test2() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for(int i = 0; i < 10; i++) {
            new Thread(() -> {
                LocalDate parse = dtf.parse("2022-05-05", LocalDate::from);
                log.debug("{}", parse);
            }).start();
        }
        TimeUtil.sleep(1);
    }


    //测试
    public static void main(String[] args) {
        Pool pool = new Pool(2);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                Connection connection = pool.getConnection();
                try {
                    Thread.sleep(new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pool.freeConnection(connection);
            }, "t" + (i + 1)).start();
        }
    }

}

//享元模式的应用 --- 连接池的实现
@Slf4j
class Pool {
    //1. 连接池的大小
    private final int poolSize;
    //2. 连接对象数组
    private Connection[] connections;
    //3. 状态数组，表示连接数组中每个连接的状态，0表示空闲，1表示繁忙
    private AtomicIntegerArray state;

    //初始化连接池
    public Pool(int poolSize) {
        this.poolSize = poolSize;
        connections = new Connection[poolSize];
        state = new AtomicIntegerArray(new int[poolSize]);

        for(int i = 0; i < poolSize; i++){
            connections[i] = new TestConnection("conn_" + (i + 1));
        }
    }

    //获取连接的方法
    public Connection getConnection() {
        //为保证线程安全，使用CAS锁的方式
        while(true) {
            for (int i = 0; i < poolSize; i++) {
                //查询每个连接的状态，如果找到空闲的连接，则返回
                if(state.get(i) == 0) {
                    //修改连接状态
                    if (state.compareAndSet(i, 0, 1)) {
                        log.debug("get: {}", connections[i]);
                        return connections[i];
                    }
                }
            }

            //如果循环一圈之后没找到，则线程进入阻塞状态，等待空闲连接
            synchronized (this) {
                try {
                    log.debug("waiting...");
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //归还连接的方法
    public void freeConnection(Connection conn) {
        //判断归还的连接是否是连接池的连接
        for (int i = 0; i < poolSize; i++) {
            if(connections[i] == conn) {
                state.set(1, 0);
                //唤醒正在等待的线程
                synchronized (this) {
                    log.debug("free: {}", conn);
                    this.notifyAll();
                }
                break;
            }
        }
    }

}

//为做测试编写一个测试的连接，都是一些空实现
class TestConnection implements Connection {
    private String name;

    public TestConnection(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestConnection{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public Statement createStatement() throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {

    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return false;
    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {

    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {

    }

    @Override
    public String getCatalog() throws SQLException {
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {

    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    @Override
    public void setHoldability(int holdability) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return false;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {

    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}

