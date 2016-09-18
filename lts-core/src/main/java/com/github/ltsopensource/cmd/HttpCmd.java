package com.github.ltsopensource.cmd;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.commons.utils.WebUtils;
import com.github.ltsopensource.core.json.JSON;

import java.io.IOException;
import java.util.Map;

/**
 * 可以实现子类,定制化返回值
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
@SuppressWarnings("unchecked")
public class HttpCmd<Resp extends HttpCmdResponse> extends HttpCmdRequest {
    /**
     * 子类不要覆盖这个
     */
    final public Resp doGet(String url) throws IOException {

        Resp resp = null;
        String result = null;
        try {
            result = WebUtils.doGet(url, null);
        } catch (IOException e1) {
            try {
                resp = (Resp) getResponseClass().newInstance();
                resp.setSuccess(false);
                resp.setMsg("GET ERROR: url=" + url + ", errorMsg=" + e1.getMessage());
                return resp;
            } catch (InstantiationException e) {
                throw new HttpCmdException(e);
            } catch (IllegalAccessException e) {
                throw new HttpCmdException(e);
            }
        }
        if (StringUtils.isNotEmpty(result)) {
            resp = JSON.parse(result, getResponseClass());
        }
        return resp;
    }

    protected Class<? extends HttpCmdResponse> getResponseClass() {
        return HttpCmdResponse.class;
    }

    public Resp doPost(String url, Map<String, String> params) {
        Resp resp = null;
        String result = null;
        try {
            result = WebUtils.doPost(url, params, 3000, 30000);
        } catch (IOException e1) {
            try {
                resp = (Resp) getResponseClass().newInstance();
                resp.setSuccess(false);
                resp.setMsg("POST ERROR: url=" + url + ", errorMsg=" + e1.getMessage());
                return resp;
            } catch (InstantiationException e) {
                throw new HttpCmdException(e);
            } catch (IllegalAccessException e) {
                throw new HttpCmdException(e);
            }
        }
        if (StringUtils.isNotEmpty(result)) {
            resp = JSON.parse(result, getResponseClass());
        }
        return resp;
    }
}
