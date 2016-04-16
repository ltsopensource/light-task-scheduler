package com.github.ltsopensource.core.commons.concurrent.limiter;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class RateLimiterTest {

    static volatile boolean stop = false;

    @Test
    public void testRateLimiter() throws InterruptedException {

        final RateLimiter rateLimiter = RateLimiter.create(4.0);

        long start = System.nanoTime();
        final AtomicLong okNum = new AtomicLong(0);
        final AtomicLong blockNum = new AtomicLong(0);

        for (int i = 0; i < 5; i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stop) {
                        if (rateLimiter.tryAcquire(1)) {
                            okNum.incrementAndGet();
                        } else {
                            blockNum.incrementAndGet();
                        }
                    }
                }
            }).start();
        }

        Thread.sleep(10 * 1000L);

        stop = true;

        System.out.println(System.nanoTime() - start);
        System.out.println("ok=" + okNum.get());
        System.out.println("block=" + blockNum.get());
    }

}