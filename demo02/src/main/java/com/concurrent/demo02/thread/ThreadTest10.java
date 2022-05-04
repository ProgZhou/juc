package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.DownLoader;
import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 保护性暂停模式
 * @author ProgZhou
 * @createTime 2022/04/27
 */
@Slf4j
public class ThreadTest10 {
    //模拟场景：线程t1等待线程t2下载结果
    @Test
    public void test1(){

        GuardedObject guardedObject = new GuardedObject();

        Thread t1 = new Thread(() -> {
            //线程1等待结果
            log.debug("waiting...");
            List<String> o = (List<String>) guardedObject.get();
            log.debug("result: {}", o.size());
        }, "t1");

        Thread t2 = new Thread(() -> {
            log.debug("download...");
            try {
                List<String> lines = DownLoader.download();
                guardedObject.complete(lines);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t2");
        t1.start();
        t2.start();

        TimeUtil.sleep(2);
    }


    //测试多版本的GuardedObject
    @Test
    public void test2(){
        for (int i = 0; i < 3; i++) {
            new People("people" + i).start();
        }
        TimeUtil.sleep(1);
        for (Integer id : MailBox.getIds()) {
            new Postman(id, "内容" + id, "postman").start();
        }
    }
}

//监控类
class GuardedObject{
    private Object response;

    //获取结果
    public Object get(){
        synchronized (this){
            //条件不满足则等待，防止虚假唤醒
            while (response == null){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }

    //产生结果
    public void complete(Object o){
        synchronized (this){
            this.response = o;

            //唤醒其他线程
            this.notifyAll();
        }
    }
}

//带超时的GuardedObject
class GuardedObjectTimeout{
    //用一个唯一id来标识GuardedObject
    private int id;

    private Object response;

    public GuardedObjectTimeout(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    //获取结果，增加超时时间
    public Object get(long timeout){
        synchronized (this){
            //记录等待的开始时间
            long begin = System.currentTimeMillis();
            //记录等待经历的时间
            long passTime = 0;
            while (response == null){
                //假设 millis 是 1000，结果在 400 时被虚假唤醒了，那么还有 600 要等
                long waitTime = timeout - passTime;
                //在每次循环时判断是否超时
                if(waitTime <= 0){
                    //如果超时了，即跳出循环
                    break;
                }
                try {
                    //这里也不能干等着，需要设置一个等待时间
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //在每一次等待结束后记录等待所经历的时间
                passTime = System.currentTimeMillis() - begin;
            }
            return response;
        }
    }

    //产生结果
    public void complete(Object o){
        synchronized (this){
            this.response = o;

            //唤醒其他线程
            this.notifyAll();
        }
    }
}

//使用一个中间类实现线程与GuardedObject之间的解耦
class MailBox{

    //用于存放每个GuardedObject的map，需要确保线程安全，所以使用HashTable
    private static Map<Integer, GuardedObjectTimeout> boxes = new Hashtable<>();

    //为每个放入boxes的GuardedObject产生一个id
    private static int id = 0;

    //需要确保线程安全
    private synchronized static int getObjectId(){
        id++;
        return id;
    }

    //根据id获取GuardedObject
    public static GuardedObjectTimeout getGuardedObject(int id){
        return boxes.get(id);
    }

    public static GuardedObjectTimeout createGuardedObject(){
        //创建一个GuardedObject并为其分配唯一id
        GuardedObjectTimeout got = new GuardedObjectTimeout(getObjectId());
        //存入boxes中
        boxes.put(got.getId(), got);

        return got;
    }

    //获取MailBoxes中的id列表
    public static Set<Integer> getIds(){
        return boxes.keySet();
    }

}

//模拟居民收信的场景
@Slf4j
class People extends Thread {

    public People(){}

    public People(String name) {
        super(name);
    }

    @Override
    public void run() {
        //在居民楼底下的信箱中创建一个属于自己的收信格
        GuardedObjectTimeout guardedObject = MailBox.createGuardedObject();
        log.debug("people: {}", guardedObject.getId());
        //假定只等待5s
        Object mail = guardedObject.get(5000);

        log.debug("people {}, receive mail: {}", guardedObject.getId(), mail);
    }
}

//模拟送信员
@Slf4j
class Postman extends Thread {
    //信件的id和信件内容，id标明需要送给几号居民
    private int id;
    private String mail;

    public Postman(){}

    public Postman(int id, String mail, String name){
        super(name);
        this.id = id;
        this.mail = mail;
    }

    @Override
    public void run() {
        //获取居民创建的收信格
        GuardedObjectTimeout guardedObject = MailBox.getGuardedObject(id);
        log.debug("send mail id: {}, content: {}", guardedObject.getId(), mail);
        //相当于一个投递的过程
        guardedObject.complete(mail);
    }
}