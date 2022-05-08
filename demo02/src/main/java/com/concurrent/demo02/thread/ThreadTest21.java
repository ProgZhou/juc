package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** 异步模式之工作线程模式
 * @author ProgZhou
 * @createTime 2022/05/08
 */
@Slf4j
public class ThreadTest21 {
    static final List<String> MENU = Arrays.asList("地三鲜", "宫保鸡丁", "辣子鸡丁", "烤鸡翅");
    static Random RANDOM = new Random();
    static String cooking() {
        return MENU.get(RANDOM.nextInt(MENU.size()));
    }
    public static void main(String[] args) {
        ExecutorService waitPool = Executors.newFixedThreadPool(1);
        ExecutorService cookPool = Executors.newFixedThreadPool(1);

        waitPool.execute(() -> {
            log.debug("处理点餐...");
            Future<String> f = cookPool.submit(() -> {
                log.debug("做菜...");
                return cooking();
            });
            try {
                log.debug("上菜：{}", f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        waitPool.execute(() -> {
            log.debug("处理点餐...");
            Future<String> f = cookPool.submit(() -> {
                log.debug("做菜...");
                return cooking();
            });
            try {
                log.debug("上菜：{}", f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

}
