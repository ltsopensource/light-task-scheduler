package com.lts.remoting.netty;

import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;


/**
 * 接收请求处理器，服务器与客户端通用
 */
public interface NettyRequestProcessor {
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException;
}
