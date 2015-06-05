package com.lts.job.remoting;

import com.lts.job.remoting.netty.ResponseFuture;


/**
 * 异步调用应答回调接口
 */
public interface InvokeCallback {
    public void operationComplete(final ResponseFuture responseFuture);
}
