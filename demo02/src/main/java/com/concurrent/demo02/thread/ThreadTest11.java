package com.concurrent.demo02.thread;

import com.concurrent.demo02.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Deque;
import java.util.LinkedList;

/** 生产者 -- 消费者问题
 * @author ProgZhou
 * @createTime 2022/04/28
 */
@Slf4j
public class ThreadTest11 {

    @Test
    public void test(){
        MessageQueue queue = new MessageQueue(2);

        //创建生产者线程
        for(int i = 0; i < 3; i++){
            int id = i;
            new Thread(() -> {
                queue.put(new Message(id, "message" + id));
            }, "producer " + i).start();
        }

        //创建消费者线程
        new Thread(() -> {
            while (true) {
                //消费者每隔1s或许一次消息
                TimeUtil.sleep(1);
                Message take = queue.take();
            }
        }, "consumer").start();

        TimeUtil.sleep(5);
    }

}

//存放消息的消息队列
@Slf4j
class MessageQueue {

    //存放消息的消息队列
    private final Deque<Message> messageQueue = new LinkedList<>();

    //消息队列的最大容量
    private int capacity;

    public MessageQueue() {
    }

    public MessageQueue(int capacity) {
        this.capacity = capacity;
    }

    //获取消息 -- 消费者对消息队列中的消息进行互斥访问
    public Message take() {
        synchronized (messageQueue){
            //使用之前的循环等待，当队列为空时，不能获取消息，等待生产者线程存入消息
            while (messageQueue.isEmpty()){
                try {
                    log.debug("queue is empty, consumer waiting...");
                    messageQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //生产者线程存入消息，并唤醒正在等待消费消息的消费者
            Message message = messageQueue.removeFirst();
            log.debug("consumer get message: {}", message.toString());
            messageQueue.notifyAll();
            return message;
        }
    }

    //存入消息 -- 生产者对消息队列中的空缓冲区进行互斥访问
    public void put(Message message){
        synchronized (messageQueue) {
            //当消息队列被占满时，生产者线程不能再向其中加入消息
            while (messageQueue.size() == capacity){
                try {
                    log.debug("queue is full, producer waiting...");
                    messageQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //生产者将消息放入消息队列
            messageQueue.addLast(message);
            log.debug("producer put message: {}", message.toString());
            //唤醒正在等待消费的消费者线程
            messageQueue.notifyAll();
        }
    }
}

//消息
@Slf4j
final class Message{
    //消息需要有唯一标识
    private int messageTag;

    //消息内容
    private Object content;

    public Message(int messageTag, Object content) {
        this.messageTag = messageTag;
        this.content = content;
    }

    public int getMessageTag() {
        return messageTag;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageTag=" + messageTag +
                ", content=" + content +
                '}';
    }
}
