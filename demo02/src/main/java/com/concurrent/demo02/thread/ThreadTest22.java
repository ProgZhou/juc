package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** 定时任务
 * @author ProgZhou
 * @createTime 2022/05/10
 */
@Slf4j
public class ThreadTest22 {
    public static void main(String[] args) {
        //问题：如何让线程在每周四18:00:00定时执行任务

        //获取当前的时间
        LocalDateTime now = LocalDateTime.now();

        //获取本周四18:00:00的时间，根据now的时间进行修改
        LocalDateTime thursday = now.withHour(18).withMinute(0).withSecond(0).withNano(0).with(DayOfWeek.THURSDAY);
        //如果当前的时间超过了本周四的18:00:00，比如现在是本周的周五，需要获取的是下周周四的时间
        if(now.compareTo(thursday) >= 0) {
            //在thursday的基础上增加一周即可
            thursday = now.plusWeeks(1);
        }
        log.debug("now: {}", now);
        log.debug("thursday: {}", thursday);

        long initialDelay = Duration.between(now, thursday).toMillis();
        long period = 1000 * 60 * 60 * 24 * 7;
        //创建定时线程池
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        /*
          Runnable command: 待执行的任务
          long initialDelay: 延时时间
          long period: 时间间隔
          TimeUnit unit: 时间单位
         */
        //以毫秒为单位
        pool.scheduleAtFixedRate(() -> {
            log.debug("running...");
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
