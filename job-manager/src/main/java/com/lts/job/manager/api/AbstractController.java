package com.lts.job.manager.api;

import com.lts.job.common.util.StringUtils;
import com.lts.job.manager.domain.RestfulResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Robert HG (254963746@qq.com) on 8/25/14.
 */
public class AbstractController {

    public String execute(Delegate delegate) {
        RestfulResponse response = new RestfulResponse();
        try {
            delegate.delegate(response);
        } catch (Throwable e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
        }

        StringBuilder result = new StringBuilder();
        String callback = getRequest().getParameter("__invoke");
        if (StringUtils.isNotEmpty(callback)) {
            result.append(callback).append("(");
        }
        result.append(response.toJsonString());
        if (StringUtils.isNotEmpty(callback)) {
            result.append(");");
        }
        return result.toString();
    }

    protected interface Delegate {
        public void delegate(RestfulResponse response);
    }

    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

}
