package com.lts.job.task.tracker.expcetion;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * 执行 任务异常  
 * 默认日志级别是Error ， 如果设置了 isWarn 就会变为Warn
 */
public class JobRunException extends Exception {

    // 错误编码
    private String code;

    public JobRunException(String code, String message) {
        super(message);
        this.code = code;
    }

    public JobRunException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }
}
