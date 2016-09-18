package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.Level;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class TaskTrackerCfgLoader {

    public static TaskTrackerCfg load(String confPath) throws CfgException {

        String cfgPath = confPath + "/tasktracker.cfg";
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

        TaskTrackerCfg cfg = new TaskTrackerCfg();
        try {
            String registryAddress = conf.getProperty("registryAddress");
            Assert.hasText(registryAddress, "registryAddress can not be null.");
            cfg.setRegistryAddress(registryAddress);

            String clusterName = conf.getProperty("clusterName");
            Assert.hasText(clusterName, "clusterName can not be null.");
            cfg.setClusterName(clusterName);

            String jobRunnerClass = conf.getProperty("jobRunnerClass");
            Assert.hasText(jobRunnerClass, "jobRunnerClass can not be null.");
            cfg.setJobRunnerClass(Class.forName(jobRunnerClass));

            String nodeGroup = conf.getProperty("nodeGroup");
            Assert.hasText(nodeGroup, "nodeGroup can not be null.");
            cfg.setNodeGroup(nodeGroup);

            String workThreads = conf.getProperty("workThreads");
            Assert.hasText(workThreads, "workThreads can not be null.");
            cfg.setWorkThreads(Integer.parseInt(workThreads));

            cfg.setDataPath(conf.getProperty("dataPath"));

            String useSpring = conf.getProperty("useSpring");
            if (StringUtils.isNotEmpty(useSpring)) {
                cfg.setUseSpring(Boolean.valueOf(useSpring));
            }

            String bizLoggerLevel = conf.getProperty("bizLoggerLevel");
            if (StringUtils.isNotEmpty(bizLoggerLevel)) {
                cfg.setBizLoggerLevel(Level.valueOf(bizLoggerLevel));
            }

            String springXmlPaths = conf.getProperty("springXmlPaths");
            if (StringUtils.isNotEmpty(springXmlPaths)) {
                // 都好分割
                String[] tmpArr = springXmlPaths.split(",");
                if (tmpArr.length > 0) {
                    String[] springXmlPathArr = new String[tmpArr.length];
                    for (int i = 0; i < tmpArr.length; i++) {
                        springXmlPathArr[i] = StringUtils.trim(tmpArr[i]);
                    }
                    cfg.setSpringXmlPaths(springXmlPathArr);
                }
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
