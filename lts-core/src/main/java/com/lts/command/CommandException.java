package com.lts.command;

/**
 * @author Robert HG (254963746@qq.com) on 10/28/15.
 */
public class CommandException extends RuntimeException{

	private static final long serialVersionUID = 7563802613921477340L;

	public CommandException() {
        super();
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }
}
