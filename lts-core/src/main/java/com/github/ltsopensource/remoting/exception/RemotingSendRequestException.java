package com.github.ltsopensource.remoting.exception;

/**
 * RPC调用中，客户端发送请求失败，抛出此异常
 */
public class RemotingSendRequestException extends RemotingException {
    private static final long serialVersionUID = 5391285827332471674L;

    public RemotingSendRequestException(String addr) {
        this(addr, null);
    }

    public RemotingSendRequestException(String addr, Throwable cause) {
        super("send request to <" + addr + "> failed", cause);
    }
}
