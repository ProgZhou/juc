package com.concurrent.demo02.theory;

/** synchronized原理
 * @author ProgZhou
 * @createTime 2022/04/26
 */
public class SynchronizedTest {
    private static final Object lock = new Object();

    private static int count = 0;

    public static void main(String[] args) {
        synchronized (lock){
            count++;
        }
    }
}
