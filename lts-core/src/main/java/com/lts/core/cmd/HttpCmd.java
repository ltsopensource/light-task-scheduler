package com.lts.core.cmd;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.commons.utils.WebUtils;
import com.lts.core.json.JSON;

import java.io.IOException;

/**
 * 可以实现子类,定制化返回值
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class HttpCmd<Resp extends HttpCmdResponse> extends HttpCmdRequest {
    /**
     * 子类不要覆盖这个
     */
    final public Resp reqSend(String url) throws IOException {

        Resp resp = null;
        String result = null;
        try {
            result = WebUtils.doGet(url, null);
        } catch (IOException e1) {
            try {
                resp = (Resp) getResponseClass().newInstance();
                resp.setSuccess(false);
                resp.setMsg("REQUEST ERROR: url=" + url + ", errorMsg=" + e1.getMessage());
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
}
