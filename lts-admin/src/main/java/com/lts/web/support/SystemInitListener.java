package com.lts.web.support;

import com.lts.core.commons.utils.StringUtils;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Robert HG (254963746@qq.com) on 9/2/15.
 */
public class SystemInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String confPath = System.getProperty("lts.admin.conf.path");
        if (StringUtils.isNotEmpty(confPath)) {
            System.out.println("lts.admin.config.path : " + confPath);
        }
        AppConfigurer.load(confPath);
        //  log4j 配置文件路径
        if (StringUtils.isNotEmpty(confPath)) {
            PropertyConfigurator.configure(confPath + "/log4j.properties");
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
