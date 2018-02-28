package com.github.ltsopensource.monitor;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MonitorCfgLoader {

    public static MonitorCfg load(String confPath) {

        String cfgPath = confPath + "/lts-monitor.cfg";
        String log4jPath = confPath + "/log4j.properties";

        Properties conf = new Properties();
        File file = new File(cfgPath);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CfgException("can not find " + cfgPath);
        }
        try {
            conf.load(is);
        } catch (IOException e) {
            throw new CfgException("Read " + cfgPath + " error.", e);
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

        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }

        return cfg;
    }

}
