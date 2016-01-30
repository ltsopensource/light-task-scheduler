package com.lts.nio.channel;

import com.lts.nio.processor.WriteMessage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hugui.hg on 1/31/16.
 */
public class WriteQueue {

    private ConcurrentLinkedQueue<WriteMessage> queue = new ConcurrentLinkedQueue<WriteMessage>();
    private volatile ReentrantLock lock = new ReentrantLock();

    public void offer(WriteMessage message) {
        queue.offer(message);
    }

    public WriteMessage peek() {
        return queue.peek();
    }

    public WriteMessage poll() {
        return queue.poll();
    }

    public boolean tryLock() {
        return lock.tryLock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
