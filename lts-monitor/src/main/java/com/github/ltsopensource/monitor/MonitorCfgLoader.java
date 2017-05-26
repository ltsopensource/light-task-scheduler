package com.github.ltsopensource.monitor;

import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MonitorCfgLoader {

    public static MonitorCfg load(String confPath) {


        String monitorConfigName = "lts-monitor.cfg";
        String cfgPath = confPath + File.separator + monitorConfigName;

//        String log4jConfigName = "log4j.properties";
//        String log4jPath = confPath + File.separator + log4jConfigName;

        Properties conf = new Properties();
        InputStream is = null;

        try {
            is = MonitorCfgLoader.class.getClassLoader().getResourceAsStream(monitorConfigName);
            conf.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CfgException("Read " + cfgPath + " error.", e);
        }finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MonitorCfg cfg = new MonitorCfg();
        try {
            String registryAddress = conf.getProperty("registryAddress");
            Assert.hasText(registryAddress, "registryAddress can not be null.");
            cfg.setRegistryAddress(registryAddress);

            String clusterName = conf.getProperty("clusterName");
            Assert.hasText(clusterName, "clusterName can not be null.");
            cfg.setClusterName(clusterName);

            String bindIp = conf.getProperty("bindIp");
            if (StringUtils.isNotEmpty(bindIp)) {
                cfg.setBindIp(bindIp);
            }

            String identity = conf.getProperty("identity");
            if (StringUtils.isNotEmpty(identity)) {
                cfg.setIdentity(identity);
            }

            Map<String, String> configs = new HashMap<String, String>();
            for (Map.Entry<Object, Object> entry : conf.entrySet()) {
                String key = entry.getKey().toString();
                if (key.startsWith("configs.")) {
                    String value = entry.getValue() == null ? null : entry.getValue().toString();
                    configs.put(key.replace("configs.", ""), value);
                }
            }

            cfg.setConfigs(configs);
        } catch (Exception e) {
            throw new CfgException(e);
        }

//        if (FileUtils.exist(log4jPath)) {
//            //  log4j 配置文件路径
//            PropertyConfigurator.configure(log4jPath);
//        }

        return cfg;
    }

}
