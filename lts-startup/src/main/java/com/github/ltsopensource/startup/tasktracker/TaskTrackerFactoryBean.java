package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.spring.TaskTrackerAnnotationFactoryBean;
import java.util.Map;
import java.util.Properties;
import org.springframework.util.CollectionUtils;

/**
 * @author Robert HG (254963746@qq.com) on 9/12/15.
 */
public class TaskTrackerFactoryBean extends TaskTrackerAnnotationFactoryBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        String cfgPath = System.getProperty("lts.tasktracker.cfg.path");

        TaskTrackerCfg cfg = TaskTrackerCfgLoader.load(cfgPath);

        setJobRunnerClass(cfg.getJobRunnerClass());
        setClusterName(cfg.getClusterName());
        setRegistryAddress(cfg.getRegistryAddress());
        setNodeGroup(cfg.getNodeGroup());
        setWorkThreads(cfg.getWorkThreads());

        Map<String, String> configMap = cfg.getConfigs();
        Properties configs = new Properties();
        if (!CollectionUtils.isEmpty(configMap)) {
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                configs.put(entry.getKey(), entry.getValue());
            }
        }
        setConfigs(configs);

        super.afterPropertiesSet();
    }
}
