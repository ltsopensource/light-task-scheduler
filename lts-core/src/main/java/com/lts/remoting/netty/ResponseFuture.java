package com.lts.remoting.netty;

import com.lts.core.support.SystemClock;
import com.lts.remoting.InvokeCallback;
import com.lts.remoting.common.SemaphoreReleaseOnlyOnce;
import com.lts.remoting.protocol.RemotingCommand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 异步请求应答封装
 */
public class ResponseFuture {
    private final int opaque;
    private final long timeoutMillis;
    private final InvokeCallback invokeCallback;
    private final long beginTimestamp = SystemClock.now();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    // 保证信号量至多至少只被释放一次
    private final SemaphoreReleaseOnlyOnce once;
    // 保证回调的callback方法至多至少只被执行一次
    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);
    private volatile RemotingCommand responseCommand;
    private volatile boolean sendRequestOK = true;
    private volatile Throwable cause;


    public ResponseFuture(int opaque, long timeoutMillis, InvokeCallback invokeCallback,
                          SemaphoreReleaseOnlyOnce once) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.once = once;
    }


    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                invokeCallback.operationComplete(this);
            }
        }
    }


    public void release() {
        if (this.once != null) {
            this.once.release();
        }
    }


    public boolean isTimeout() {
        long diff = SystemClock.now() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }


    public RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }


    public void putResponse(final RemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
        this.countDownLatch.countDown();
    }


    public long getBeginTimestamp() {
        return beginTimestamp;
    }


    public boolean isSendRequestOK() {
        return sendRequestOK;
    }


    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }


    public long getTimeoutMillis() {
        return timeoutMillis;
    }


    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }


    public Throwable getCause() {
        return cause;
    }


    public void setCause(Throwable cause) {
        this.cause = cause;
    }


    public RemotingCommand getResponseCommand() {
        return responseCommand;
    }


    public void setResponseCommand(RemotingCommand responseCommand) {
        this.responseCommand = responseCommand;
    }


    public int getOpaque() {
        return opaque;
    }


    @Override
    public String toString() {
        return "ResponseFuture [responseCommand=" + responseCommand + ", sendRequestOK=" + sendRequestOK
                + ", cause=" + cause + ", opaque=" + opaque + ", timeoutMillis=" + timeoutMillis
                + ", invokeCallback=" + invokeCallback + ", beginTimestamp=" + beginTimestamp
                + ", countDownLatch=" + countDownLatch + "]";
    }
}
