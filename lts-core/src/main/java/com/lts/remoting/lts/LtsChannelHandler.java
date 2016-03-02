package com.lts.remoting.lts;

import com.lts.nio.handler.IoFuture;
import com.lts.nio.handler.IoFutureListener;
import com.lts.remoting.ChannelHandler;
import com.lts.remoting.ChannelHandlerListener;
import com.lts.remoting.Future;

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
