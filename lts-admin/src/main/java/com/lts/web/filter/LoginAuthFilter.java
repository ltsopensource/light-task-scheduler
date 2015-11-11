package com.lts.web.filter;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by ztajy on 2015-11-11.
 */
public class LoginAuthFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoginAuthFilter.class);

    private static final String AUTH_PREFIX = "Basic ";

    private String username = "admin";

    private String password = "admin";

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        String configFilePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + System.getProperty("file.separator") + filterConfig.getInitParameter("auth-config");
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFilePath));
        } catch (final IOException ex) {
            log.warn("Cannot found auth config file, use default auth config.");
        }
        username = props.getProperty("console.username", username);
        password = props.getProperty("console.password", password);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authorization = httpRequest.getHeader("authorization");
        if (null != authorization && authorization.length() > AUTH_PREFIX.length()) {
            authorization = authorization.substring(AUTH_PREFIX.length(), authorization.length());
            if ((username + ":" + password).equals(new String(Base64.decodeBase64(authorization)))) {
                authenticateSuccess(httpResponse);
                chain.doFilter(httpRequest, httpResponse);
            } else {
                needAuthenticate(httpRequest, httpResponse);
            }
        } else {
            needAuthenticate(httpRequest, httpResponse);
        }
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
