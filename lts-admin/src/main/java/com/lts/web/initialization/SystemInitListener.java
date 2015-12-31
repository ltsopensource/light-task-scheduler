package com.lts.web.initialization;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.json.JSONFactory;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.spi.SpiKey;
import com.lts.web.support.AppConfigurer;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Robert HG (254963746@qq.com) on 9/2/15.
 */
public class SystemInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String confPath = servletContextEvent.getServletContext().getInitParameter("lts.admin.config.path");
        if (StringUtils.isNotEmpty(confPath)) {
            System.out.println("lts.admin.config.path : " + confPath);
        }
        AppConfigurer.load(confPath);

        String jsonAdapter = AppConfigurer.getProperty("configs." + SpiKey.LTS_JSON);
        if (StringUtils.isNotEmpty(jsonAdapter)) {
            JSONFactory.setJSONAdapter(jsonAdapter);
        }

        String loggerAdapter = AppConfigurer.getProperty("configs." + SpiKey.LTS_LOGGER);
        if (StringUtils.isNotEmpty(loggerAdapter)) {
            LoggerFactory.setLoggerAdapter(loggerAdapter);
        }

        //  log4j 配置文件路径
        if (StringUtils.isNotEmpty(confPath)) {
            PropertyConfigurator.configure(confPath + "/log4j.properties");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
