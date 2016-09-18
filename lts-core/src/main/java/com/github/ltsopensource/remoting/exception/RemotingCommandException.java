package com.github.ltsopensource.remoting.exception;

/**
 * 命令解析自定义字段时，校验字段有效性抛出异常
 */
public class RemotingCommandException extends RemotingException {
    private static final long serialVersionUID = -6061365915274953096L;

    public RemotingCommandException(String message) {
        super(message, null);
    }

    public RemotingCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
