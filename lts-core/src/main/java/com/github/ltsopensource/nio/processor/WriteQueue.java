package com.github.ltsopensource.nio.processor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robert HG (254963746@qq.com) on 1/31/16.
 */
public class WriteQueue {

    private ConcurrentLinkedQueue<WriteRequest> queue = new ConcurrentLinkedQueue<WriteRequest>();
    private volatile ReentrantLock lock = new ReentrantLock();

    public void offer(WriteRequest message) {
        queue.offer(message);
    }

    public WriteRequest peek() {
        return queue.peek();
    }

    public WriteRequest poll() {
        return queue.poll();
    }

    public boolean tryLock() {
        return lock.tryLock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

}
