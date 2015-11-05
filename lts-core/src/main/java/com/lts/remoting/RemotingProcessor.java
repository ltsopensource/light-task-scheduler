package com.lts.remoting;

import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;

/**
 * 接收请求处理器，服务器与客户端通用
 */
public interface RemotingProcessor {
    public RemotingCommand processRequest(Channel channel, RemotingCommand request)
            throws RemotingCommandException;
}
