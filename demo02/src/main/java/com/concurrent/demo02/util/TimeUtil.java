package com.concurrent.demo02.util;

/** 时间工具类
 * @author ProgZhou
 * @createTime 2022/04/23
 */
public abstract class TimeUtil {

    public static void sleep(int seconds){
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
