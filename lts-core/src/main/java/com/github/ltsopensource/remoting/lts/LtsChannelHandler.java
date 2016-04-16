package com.github.ltsopensource.remoting.lts;

import com.github.ltsopensource.nio.handler.IoFuture;
import com.github.ltsopensource.nio.handler.IoFutureListener;
import com.github.ltsopensource.remoting.ChannelHandler;
import com.github.ltsopensource.remoting.ChannelHandlerListener;
import com.github.ltsopensource.remoting.Future;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsChannelHandler implements ChannelHandler {

    private IoFuture future;

    public LtsChannelHandler(IoFuture future) {
        this.future = future;
    }

    @Override
    public ChannelHandler addListener(final ChannelHandlerListener listener) {
        future.addListener(new IoFutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                listener.operationComplete(future);
            }
        });
        return this;
    }
}
