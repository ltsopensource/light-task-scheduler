package com.github.ltsopensource.nio.idle;

/**
 * @author Robert HG (254963746@qq.com) on 2/15/16.
 */
public class IdleInfo {

    private volatile long lastReadTime = 0;
    private volatile long lastWriteTime = 0;

    private volatile long lastReadIdleTime = 0;
    private volatile long lastWriteIdleTime = 0;
    private volatile long lastBothIdleTime = 0;

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(long lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }

    public long getLastReadIdleTime() {
        return lastReadIdleTime;
    }

    public void setLastReadIdleTime(long lastReadIdleTime) {
        this.lastReadIdleTime = lastReadIdleTime;
    }

    public long getLastWriteIdleTime() {
        return lastWriteIdleTime;
    }

    public void setLastWriteIdleTime(long lastWriteIdleTime) {
        this.lastWriteIdleTime = lastWriteIdleTime;
    }

    public long getLastBothIdleTime() {
        return lastBothIdleTime;
    }

    public void setLastBothIdleTime(long lastBothIdleTime) {
        this.lastBothIdleTime = lastBothIdleTime;
    }
}
