package com.lts.nio.idle;

import com.lts.core.commons.collect.ConcurrentHashSet;
import com.lts.core.support.SystemClock;
import com.lts.nio.channel.NioChannel;

import java.util.Iterator;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class IdleDetector {

    private final ConcurrentHashSet<NioChannel> channels = new ConcurrentHashSet<NioChannel>();

    public void addChannel(NioChannel channel) {
        channels.add(channel);
    }

    public void removeChannel(NioChannel channel) {
        channels.remove(channel);
    }

    private class DetectorTask implements Runnable {

        private volatile boolean stop = false;
        private volatile Thread thread;

        @Override
        public void run() {
            thread = Thread.currentThread();

            while (!stop) {

                long currentTime = SystemClock.now();

                checkIdles(currentTime);

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                }
            }
        }

        public void stop() {
            stop = true;
            Thread thread = this.thread;
            if (thread != null) {
                thread.interrupt();
            }
        }

        private void checkIdles(long currentTime) {
            Iterator<NioChannel> it = channels.iterator();
            while (it.hasNext()) {
                NioChannel channel = it.next();
                if (channel.isConnected()) {

                }
            }
        }
    }


}
