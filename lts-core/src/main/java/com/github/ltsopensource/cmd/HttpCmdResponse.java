package com.github.ltsopensource.cmd;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 2/17/16.
 */
public class HttpCmdResponse implements Serializable {

    private boolean success = false;
    private String msg;
    private String code;
    private String obj;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public static HttpCmdResponse newResponse(boolean success, String msg) {
        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(success);
        response.setMsg(msg);
        return response;
    }
}
