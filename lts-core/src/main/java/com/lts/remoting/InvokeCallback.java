package com.lts.remoting;

import com.lts.remoting.netty.ResponseFuture;


/**
 * 异步调用应答回调接口
 */
public interface InvokeCallback {
    public void operationComplete(final ResponseFuture responseFuture);
}
