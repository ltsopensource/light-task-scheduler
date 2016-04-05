package com.lts.admin.support;

import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.json.JSONFactory;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.spi.SpiExtensionKey;
import com.lts.monitor.MonitorAgentStartup;
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

        String jsonAdapter = AppConfigurer.getProperty("configs." + SpiExtensionKey.LTS_JSON);
        if (StringUtils.isNotEmpty(jsonAdapter)) {
            JSONFactory.setJSONAdapter(jsonAdapter);
        }

        String loggerAdapter = AppConfigurer.getProperty("configs." + SpiExtensionKey.LTS_LOGGER);
        if (StringUtils.isNotEmpty(loggerAdapter)) {
            LoggerFactory.setLoggerAdapter(loggerAdapter);
        }

        String log4jPath = confPath + "/log4j.properties";
        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }

        boolean monitorAgentEnable = Boolean.valueOf(AppConfigurer.getProperty("lts.monitorAgent.enable", "true"));
        if (monitorAgentEnable) {
            String ltsMonitorCfgPath = confPath;
            if (StringUtils.isEmpty(ltsMonitorCfgPath)) {
                ltsMonitorCfgPath = this.getClass().getResource("/").getPath();
                // 替换window下空格问题
                ltsMonitorCfgPath = ltsMonitorCfgPath.replaceAll("%20", " ");
            }
            MonitorAgentStartup.start(ltsMonitorCfgPath);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MonitorAgentStartup.stop();
    }
}
