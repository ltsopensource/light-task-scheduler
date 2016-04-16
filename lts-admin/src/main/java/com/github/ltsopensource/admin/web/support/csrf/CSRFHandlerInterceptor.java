package com.github.ltsopensource.admin.web.support.csrf;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Spring MVC <code>HandlerInterceptor</code> which is responsible to enforce CSRF token validity on incoming posts
 * requests. The interceptor should be registered with Spring MVC servlet using the following syntax:
 * <p/>
 * <mvc:interceptors>
 * <bean class="com.github.ltsopensource.web.support.csrf.CSRFHandlerInterceptor"/>
 * </mvc:interceptors>
 *
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public class CSRFHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof DefaultServletHttpRequestHandler) {
            return true;
        }

        if (request.getMethod().equalsIgnoreCase("GET")) {
            return true;
        } else {
            String sessionToken = CSRFTokenManager.getToken(request.getSession());
            String requestToken = CSRFTokenManager.getToken(request);
            // 检查 csrf token是否正确
            if (sessionToken.equals(requestToken)) {
                return true;
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad or missing CSRF value");
                return false;
            }
        }
    }
}
