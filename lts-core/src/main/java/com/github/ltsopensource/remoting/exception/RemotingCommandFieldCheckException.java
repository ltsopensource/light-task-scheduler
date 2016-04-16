package com.github.ltsopensource.remoting.exception;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public class RemotingCommandFieldCheckException extends Exception{

	private static final long serialVersionUID = -3040346783583325400L;

	public RemotingCommandFieldCheckException(String message) {
        super(message);
    }

    public RemotingCommandFieldCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
