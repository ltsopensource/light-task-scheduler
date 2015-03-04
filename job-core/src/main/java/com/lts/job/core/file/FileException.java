package com.lts.job.core.file;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class FileException extends Exception {

    public static final int FILE_CREATE = 1;
    public static final int FILE_CONTENT_ADD = 2;
    public static final int FILE_CONTENT_GET = 3;
    public static final int FILE_CONTENT_EMPTY = 4;

    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public FileException() {
        super();
    }

    public FileException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public FileException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }
}
