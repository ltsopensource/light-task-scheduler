package com.github.ltsopensource.admin.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class AjaxUtils {

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null && "XMLHttpRequest".equals(requestedWith);
    }

}
