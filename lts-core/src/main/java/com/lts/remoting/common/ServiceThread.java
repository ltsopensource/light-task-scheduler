package com.lts.remoting.common;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;


/**
 * 后台服务线程基类
 */
public abstract class ServiceThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);
    // 线程回收时间，默认90S
    private static final long JoinTime = 90 * 1000;
    // 执行线程
    protected final Thread thread;
    // 是否已经被Notify过
    protected volatile boolean hasNotified = false;
    // 线程是否已经停止
    protected volatile boolean stoped = false;

    public ServiceThread() {
        this.thread = new Thread(this, this.getServiceName());
    }

    public abstract String getServiceName();

    public void start() {
        this.thread.start();
    }

    public void shutdown() {
        this.shutdown(false);
    }

    public void stop() {
        this.stop(false);
    }

    public void makeStop() {
        this.stoped = true;
        logger.info("makestop thread " + this.getServiceName());
    }

    public void stop(final boolean interrupt) {
        this.stoped = true;
        logger.info("stop thread " + this.getServiceName() + " interrupt " + interrupt);
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        if (interrupt) {
            this.thread.interrupt();
        }
    }

    public void shutdown(final boolean interrupt) {
        this.stoped = true;
        logger.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        try {
            if (interrupt) {
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();
            this.thread.join(this.getJointime());
            long eclipseTime = System.currentTimeMillis() - beginTime;
            logger.info("join thread " + this.getServiceName() + " eclipse time(ms) " + eclipseTime + " "
                    + this.getJointime());
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void wakeup() {
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }
    }

    protected void waitForRunning(long interval) {
        synchronized (this) {
            if (this.hasNotified) {
                this.hasNotified = false;
                this.onWaitEnd();
                return;
            }

            try {
                this.wait(interval);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } finally {
                this.hasNotified = false;
                this.onWaitEnd();
            }
        }
    }

    protected void onWaitEnd() {
    }

    public boolean isStoped() {
        return stoped;
    }


    public long getJointime() {
        return JoinTime;
    }
}
