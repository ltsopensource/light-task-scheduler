package com.github.ltsopensource.remoting;

import java.util.EventListener;

/**
 * @author Robert HG (254963746@qq.com) on 11/3/15.
 */
public interface ChannelHandlerListener extends EventListener {

    void operationComplete(Future future) throws Exception;
}
