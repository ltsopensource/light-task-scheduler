package com.lts.web.support;

import com.lts.core.constant.Constants;
import org.h2.server.web.WebServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * H2 Console 的 servlet
 *
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public class H2ConsoleWebServlet extends WebServlet {

	private static final long serialVersionUID = 7664716645859452731L;

	@Override
    public void init(ServletConfig config) throws ServletException {
        ServletConfigFacade servletConfigFacade = new ServletConfigFacade(config);

        String monitorDBPath = AppConfigurer.getProperty("lts.admin.data.path",
                Constants.USER_HOME) + "/.lts/h2/lts-admin";
        // http://h2database.com/html/features.html#connection_modes
        // http://h2database.com/html/features.html#auto_mixed_mode
        String url = "jdbc:h2:" + monitorDBPath + ";AUTO_SERVER=TRUE";
        servletConfigFacade.setInitParameter("url", url);
        servletConfigFacade.setInitParameter("user", "lts");
        servletConfigFacade.setInitParameter("password", "");
        servletConfigFacade.setInitParameter("webAllowOthers", "true");

        super.init(servletConfigFacade);
    }
}

/**
 * 主要为解决 jetty embedded 的问题
 */
class ServletConfigFacade implements ServletConfig {

    private ServletConfig servletConfig;

    private Map<String, String> initParams;

    public ServletConfigFacade(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        this.initParams = new HashMap<String, String>();

        initParams();
    }

    private void initParams() {
        Enumeration<?> en = servletConfig.getInitParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement().toString();
            String value = servletConfig.getInitParameter(name);
            initParams.put(name, value);
        }
    }

    @Override
    public String getServletName() {
        return servletConfig.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletConfig.getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParams.keySet());
    }

    public void setInitParameter(String name, String value) {
        initParams.put(name, value);
    }
}