package com.concurrent.demo02.thread;

import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** ConcurrentHashMap
 * @author ProgZhou
 * @createTime 2022/05/15
 */
public class ThreadTest27 {

    static final String ALPHA = "abcedfghijklmnopqrstuvwxyz";

    public static void main(String[] args) {
        int length = ALPHA.length();
        int count = 200;
        List<String> list = new ArrayList<>(length * count);
        for (int i = 0; i < length; i++) {
            char ch = ALPHA.charAt(i);
            for (int j = 0; j < count; j++) {
                list.add(String.valueOf(ch));
            }
        }
        Collections.shuffle(list);
        for (int i = 0; i < 26; i++) {
            try (PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream("D:/Java_idea/JavaConcurrent/demo02/src/tmp/" +
                                    (i + 1) + ".txt")))) {
                String collect = list.subList(i * count, (i + 1) * count).stream()
                        .collect(Collectors.joining("\n"));
                out.print(collect);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void test1() {
        for (int i = 0; i < 26; i++) {
            File file = new File("D:/Java_idea/JavaConcurrent/demo02/src/tmp/" +
                    (i + 1) + ".txt");
            if(!file.exists()) {
                try {
                    boolean newFile = file.createNewFile();
                    System.out.println(newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //正确地创建demo函数的两个参数，使得单词统计的结果正确
    //1. 提供一个 map 集合，用来存放每个单词的计数结果，key 为单词，value 为计数
    //2. 提供一组操作，保证计数的安全性，会传递 map 集合以及 单词 List
    @Test
    public void test2() {
        //如果使用HashMap
        demo(
                () -> new ConcurrentHashMap<String, LongAdder>() {
                },

                (map, words) -> {
                    for (String word : words) {

                        //如果map中不含有这个key，则生成一个value，然后将<key,value>存到map中
                        LongAdder value = map.computeIfAbsent(word, (key) -> new LongAdder());
                        //执行累加
                        value.increment();
//                        Integer counter = map.get(word);
//                        int newValue = counter == null ? 1 : counter + 1;
//                        map.put(word, newValue);
                    }
                });
    }

    //模版代码，模版代码中封装了多线程读取文件的代码
    public static <V> void demo(Supplier<Map<String,V>> supplier,
                                 BiConsumer<Map<String,V>,List<String>> consumer) {
        Map<String, V> counterMap = supplier.get();
        List<Thread> ts = new ArrayList<>();
        for (int i = 1; i <= 26; i++) {
            int idx = i;
            Thread thread = new Thread(() -> {
                List<String> words = readFromFile(idx);
                consumer.accept(counterMap, words);
            });
            ts.add(thread);
        }
        ts.forEach(t -> t.start());
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(counterMap);


    }

    public static List<String> readFromFile(int i) {
        ArrayList<String> words = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("D:/Java_idea/JavaConcurrent/demo02/src/tmp/"
                + i +".txt")))) {
            while(true) {
                String word = in.readLine();
                if(word == null) {
                    break;
                }
                words.add(word);
            }
            return words;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
