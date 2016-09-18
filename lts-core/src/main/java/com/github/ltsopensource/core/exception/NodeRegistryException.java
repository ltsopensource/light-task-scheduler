package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class NodeRegistryException extends RuntimeException {

	private static final long serialVersionUID = 3113597129620580907L;

	public NodeRegistryException() {
        super();
    }

    public NodeRegistryException(String message) {
        super(message);
    }

    public NodeRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeRegistryException(Throwable cause) {
        super(cause);
    }

}
