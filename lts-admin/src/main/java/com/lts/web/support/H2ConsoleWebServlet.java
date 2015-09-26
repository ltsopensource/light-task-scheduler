package com.lts.web.support;

import com.lts.core.constant.Constants;
import org.h2.server.web.WebServlet;

import javax.servlet.ServletContext;

/**
 * @author Robert HG (254963746@qq.com) on 9/26/15.
 */
public class H2ConsoleWebServlet extends WebServlet {

    @Override
    public void init() {

        ServletContext servletContext = this.getServletConfig().getServletContext();
        String monitorDBPath = AppConfigurer.getProperties("lts.admin.data.path",
                Constants.USER_HOME) + "/.lts/h2/lts-admin";
        // http://h2database.com/html/features.html#connection_modes
        // http://h2database.com/html/features.html#auto_mixed_mode
        String url = "jdbc:h2:" + monitorDBPath+";AUTO_SERVER=TRUE";
        servletContext.setInitParameter("url", url);
        servletContext.setInitParameter("user", "lts");
        servletContext.setInitParameter("password", "");

        super.init();
    }
}
