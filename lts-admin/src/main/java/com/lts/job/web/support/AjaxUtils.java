package com.lts.job.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hugui on 6/7/15.
 */
public class AjaxUtils {

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
    }

}
