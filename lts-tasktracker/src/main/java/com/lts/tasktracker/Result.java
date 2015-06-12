package com.lts.tasktracker;

/**
 * Created by hugui on 6/12/15.
 */
public class Result {

    private boolean success;

    private String msg;

    public Result() {
    }

    public Result(boolean success) {
        this.success = success;
    }

    public Result(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
