package com.github.ltsopensource.admin.web.support.csrf;

import javax.servlet.http.HttpServletRequest;

/**
 * 配置在 velocity tools 中
 *
 * <input type="hidden" name="csrfToken" value="$csrfTool.getToken($request)"/>
 *
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public class CSRFTool {
    public static String getToken(HttpServletRequest request) {
        return CSRFTokenManager.getToken(request.getSession());
    }
}
