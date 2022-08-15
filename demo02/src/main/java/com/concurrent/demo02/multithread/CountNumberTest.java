package com.concurrent.demo02.multithread;

import java.util.*;
import java.util.concurrent.*;

/** 模拟多线程统计大数据量ip，这里ip使用随机数代替
 * @author ProgZhou
 * @createTime 2022/08/15
 */
public class CountNumberTest {

    public List<List<Integer>> resource = new ArrayList<>();

    //初始化，resource代表待统计的资源的集合
    public CountNumberTest() {
        //这里模拟有五个待统计的资源集合
        for(int i = 1; i <= 9; i++) {
            resource.add(new ArrayList<>());
        }
    }

    //模拟数据，这里分别给五个集合添加1000000条数据，统计每个数字出现的次数
    public void addData() {
        for (List<Integer> list : resource) {
            for(int i = 1; i <= 1000000; i++) {
                int num = new Random().nextInt(5000);
                list.add(num);
            }
        }
    }

    //统计方法，传入参数为线程个数
    public Map<Integer, Integer> count(int thread) throws ExecutionException, InterruptedException {
        Map<Integer, Integer> result = new HashMap<>();
        //创建固定大小的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(thread);
        //采用Future类任务
        List<Future<Map<Integer, Integer>>> workList = new ArrayList<>();
        for (List<Integer> list : resource) {
//            for(int i = 0; i < 5; i++) {
            //线程池中的每一个线程负责统计一个集合
                workList.add(threadPool.submit(new JobWorker(list)));
            //}
        }

        //将最后的结果合并
        for (Future<Map<Integer, Integer>> future : workList) {
            Map<Integer, Integer> countMap = future.get();
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                result.put(entry.getKey(), result.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        threadPool.shutdown();
        return result;
    }

    //测试程序
    public static void main(String[] args) {
        CountNumberTest test = new CountNumberTest();
        test.addData();
        for (List<Integer> list : test.resource) {
            System.out.println(list.size());
        }
        try {
            long start = System.currentTimeMillis();
            Map<Integer, Integer> multiRes = test.count(5);
            long end = System.currentTimeMillis();
            int sum = 0;
            for (Integer key : multiRes.keySet()) {
                sum += multiRes.get(key);
                System.out.println(key + " : " + multiRes.get(key));
            }
            System.out.println(sum);
            System.out.println("共" + (end - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("******************************************");
//        Map<Integer, Integer> singleRes = new HashMap<>();
//        long start = System.currentTimeMillis();
//        for (List<Integer> list : test.resource) {
//            for (Integer key : list) {
//                singleRes.put(key, singleRes.getOrDefault(key, 0) + 1);
//            }
//        }
//        long end = System.currentTimeMillis();
//        for (Integer key : singleRes.keySet()) {
//            System.out.println(key + " : " + singleRes.get(key));
//        }
//        System.out.println("共" + (end - start) + "ms");
    }

    //将统计的方法单独提取
    static class JobWorker implements Callable<Map<Integer, Integer>> {

        private List<Integer> list;

        public JobWorker(List<Integer> list) {
            this.list = list;
        }

        @Override
        public Map<Integer, Integer> call() throws Exception {
            Map<Integer, Integer> map = new HashMap<>();
            for (int data : list) {
                map.put(data, map.getOrDefault(data, 0) + 1);
            }
            return map;
        }
    }

}
