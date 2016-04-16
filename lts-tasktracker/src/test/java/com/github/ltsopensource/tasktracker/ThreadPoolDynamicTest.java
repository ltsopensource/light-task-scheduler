package com.github.ltsopensource.tasktracker;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class ThreadPoolDynamicTest {

    public static void main(String[] args) {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),           // 直接提交给线程而不保持它们
                new ThreadPoolExecutor.AbortPolicy());      // A handler for rejected tasks that throws a


        for (int i = 1; i < 12; i++) {
            try {
                final int finalI = i;
                threadPoolExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("第" + finalI + "个开始执行");
                            Thread.sleep(1000000000L);
                        } catch (InterruptedException e) {
                            System.out.println("第" + finalI + "个执行结果失败");
                        }
                    }
                });
                System.out.println("第" + i + "个提交成功");
            } catch (Exception e) {
                System.out.println("第" + i + "个提交失败");
            }
        }

        threadPoolExecutor.setMaximumPoolSize(20);

        for (int i = 12; i < 20; i++) {
            try {
                final int finalI = i;
                threadPoolExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("第" + finalI + "个开始执行");
                            Thread.sleep(1000000000L);
                        } catch (InterruptedException e) {
                            System.out.println("第" + finalI + "个执行结果失败");
                        }
                    }
                });
                System.out.println("第" + i + "个提交成功");
            } catch (Exception e) {
                System.out.println("第" + i + "个提交失败");
            }
        }

    }

}
