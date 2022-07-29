package com.chuangcius.design;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * StockDemo
 *
 * @author xugang.song
 * @date 2022.05.31
 */
public class StockDemo {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final int REQUEST_QUEUE_SIZE = 5;
    private static final BlockingQueue<RequestPromise> REQUEST_QUEUE = new LinkedBlockingQueue<>(REQUEST_QUEUE_SIZE);
    private static Integer stock = 10;

    public static void main(String[] args) throws InterruptedException {
        mergeJob();
        Thread.sleep(2000);

        List<Future<Result>> futureList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < REQUEST_QUEUE_SIZE; i++) {
            final Long userId = (long) i;
            final Long orderId = i + 100L;
            Future<Result> future = executorService.submit(() -> {
                countDownLatch.countDown();
                return operate(new UserRequest(orderId, userId, 1));
            });
            futureList.add(future);
        }
        futureList.forEach(future -> {
            try {
                Result result = future.get(300, TimeUnit.MILLISECONDS);
                System.out.println(Thread.currentThread().getName() + ":client response:" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static Result operate(UserRequest userRequest) throws InterruptedException {
        RequestPromise requestPromise = new RequestPromise(userRequest);
        synchronized (requestPromise) {
            boolean enqueueSuccess = REQUEST_QUEUE.offer(requestPromise, 100, TimeUnit.MILLISECONDS);
            if (!enqueueSuccess) {
                return new Result(false, "system is busy now!");
            }
            try {
                requestPromise.wait(200);
                if (requestPromise.getResult() == null) {
                    return new Result(false, "waiting timeout");
                }
            } catch (InterruptedException exception) {
                return new Result(false, "interrupted");
            }
        }
        return requestPromise.getResult();
    }

    public static void mergeJob() {
        Executors.newSingleThreadExecutor().submit(() -> {
            List<RequestPromise> list = new ArrayList<>();
            while (true) {
                if (REQUEST_QUEUE.isEmpty()) {
                    try {
                        Thread.sleep(10);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                int batchSize = REQUEST_QUEUE.size();
                for (int i = 0; i < batchSize; i++) {
                    list.add(REQUEST_QUEUE.poll());
                }
                System.out.println(Thread.currentThread().getName() + ":deduce stock:" + list);

                int sum = list.stream().mapToInt(requestPromise -> requestPromise.getUserRequest().getCount()).sum();
                if (sum <= stock) {
                    stock -= sum;
                    list.forEach(requestPromise -> {
                        requestPromise.setResult(new Result(true, "ok"));
                        synchronized (requestPromise) {
                            requestPromise.notify();
                        }
                    });
                    list.clear();
                    continue;
                }
                for (RequestPromise requestPromise : list) {
                    int count = requestPromise.getUserRequest().getCount();
                    if (count <= stock) {
                        stock -= count;
                        requestPromise.setResult(new Result(true, "ok"));
                    } else {
                        requestPromise.setResult(new Result(false, "insufficient stock"));
                    }
                    synchronized (requestPromise) {
                        requestPromise.notify();
                    }
                }
                list.clear();
            }
        });
    }
}
