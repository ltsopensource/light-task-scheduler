package com.lts.job.manager.domain;

import com.lts.job.common.util.JsonUtils;

/**
 * @author Robert HG (254963746@qq.com) on 8/25/14.
 */
public class RestfulResponse {

    private Boolean success = true;

    private String msg;

    private Object body;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String toJsonString() {
        return JsonUtils.objectToJsonString(this);
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
