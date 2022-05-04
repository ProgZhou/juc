package com.concurrent.demo02.thread;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;

/** 偏向锁
 * @author ProgZhou
 * @createTime 2022/04/26
 */
@Slf4j
public class ThreadTest8 {
    public static void main(String[] args) {
        Dog dog = new Dog();

        dog.hashCode();   //调用对象的hashCode方法会禁用偏向锁

        String s = ClassLayout.parseInstance(dog).toPrintable();
        log.debug(s);

        synchronized (dog){
            log.debug(ClassLayout.parseInstance(dog).toPrintable());
        }
        log.debug(ClassLayout.parseInstance(dog).toPrintable());
    }
}
class Dog{

}
