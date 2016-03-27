package com.lts.admin.web.support;

import com.lts.admin.web.vo.RestfulResponse;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public class Builder {


    public static RestfulResponse build(boolean success, String msg) {
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(success);
        response.setMsg(msg);
        return response;
    }

    public static RestfulResponse build(boolean success) {
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(success);
        return response;
    }
}
