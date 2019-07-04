package com.github.ltsopensource.startup.jobtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.util.StringUtils;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class JobTrackerCfgLoader {

    public static JobTrackerCfg load(String confPath) throws CfgException {

        String cfgPath = confPath + "/jobtracker.cfg";
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

        JobTrackerCfg cfg = new JobTrackerCfg();
        String registryAddress = conf.getProperty("registryAddress");
        if (StringUtils.isEmpty(registryAddress)) {
            throw new CfgException("registryAddress can not be null.");
        }
        cfg.setRegistryAddress(registryAddress);

        String clusterName = conf.getProperty("clusterName");
        if (StringUtils.isEmpty(clusterName)) {
            throw new CfgException("clusterName can not be null.");
        }
        cfg.setClusterName(clusterName);

        String bindIp = conf.getProperty("bindIp");
        if (!StringUtils.isEmpty(clusterName)) {
            cfg.setBindIp(bindIp);
        }

        String listenPort = conf.getProperty("listenPort");
        if (StringUtils.isEmpty(listenPort)) { // FIXME and is numeric
            throw new CfgException("listenPort can not be null.");
        }
        cfg.setListenPort(Integer.parseInt(listenPort));

        Map<String, String> configs = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : conf.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("configs.")) {
                String value = entry.getValue() == null ? null : entry.getValue().toString();
                configs.put(key.replace("configs.", ""), value);
            }
        }

        cfg.setConfigs(configs);

        if (Files.exists(Paths.get(log4jPath))) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }
        return cfg;
    }

}
