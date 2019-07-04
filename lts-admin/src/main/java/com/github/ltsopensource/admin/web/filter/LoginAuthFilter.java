package com.github.ltsopensource.admin.web.filter;

import com.github.ltsopensource.admin.support.AppConfigurer;
import java.io.IOException;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.AntPathMatcher;

/**
 * Created by ztajy on 2015-11-11.
 *
 * @author ztajy
 * @author Robert HG (254963746@qq.com)
 */
public class LoginAuthFilter implements Filter {
    private static final String AUTH_PREFIX = "Basic ";
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    private String username = "admin";

    private String password = "admin";

    private String[] excludedURLArray;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        username = AppConfigurer.getProperty("console.username", username);
        password = AppConfigurer.getProperty("console.password", password);

        String excludedURLs = filterConfig.getInitParameter("excludedURLs");
        if (StringUtils.isNotEmpty(excludedURLs)) {
            String[] arr = excludedURLs.split(",");
            excludedURLArray = new String[arr.length];
            for (int i = 0; i < arr.length; i++) {
                excludedURLArray[i] = StringUtils.trim(arr[i]);
            }
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isExclude(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String authorization = httpRequest.getHeader("authorization");
        if (null != authorization && authorization.length() > AUTH_PREFIX.length()) {
            authorization = authorization.substring(AUTH_PREFIX.length());
            if ((username + ":" + password).equals(new String(Base64.getDecoder().decode(authorization)))) {
                authenticateSuccess(httpResponse);
                chain.doFilter(httpRequest, httpResponse);
            } else {
                needAuthenticate(httpRequest, httpResponse);
            }
        } else {
            needAuthenticate(httpRequest, httpResponse);
        }
    }

    private boolean isExclude(String path) {
        if (excludedURLArray != null) {
            for (String page : excludedURLArray) {
                //判断是否在过滤url中
                if (pathMatcher.match(page, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void authenticateSuccess(final HttpServletResponse response) {
        response.setStatus(200);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
    }

    private void needAuthenticate(final HttpServletRequest request, final HttpServletResponse response) {
        response.setStatus(401);
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("WWW-authenticate", AUTH_PREFIX + "Realm=\"lts admin need auth\"");
    }

    @Override
    public void destroy() {
    }
}
