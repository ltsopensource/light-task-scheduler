package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.autoconfigure.PropertiesConfigurationFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.properties.JobClientProperties;
import com.github.ltsopensource.core.properties.TaskTrackerProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class QuartzLTSProxyBean implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzLTSProxyBean.class);
    // 是否使用LTS
    private boolean ltsEnable = true;

    private String[] locations;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (ltsEnable) {
            // 如果启用才进行代理
            LOGGER.info("========LTS====== Proxy Quartz Scheduler");

            JobClientProperties jobClientProperties = PropertiesConfigurationFactory.createPropertiesConfiguration(JobClientProperties.class, locations);
            jobClientProperties.checkProperties();

            TaskTrackerProperties taskTrackerProperties = PropertiesConfigurationFactory.createPropertiesConfiguration(TaskTrackerProperties.class, locations);
            taskTrackerProperties.checkProperties();

            QuartzLTSConfig quartzLTSConfig = new QuartzLTSConfig();
            quartzLTSConfig.setJobClientProperties(jobClientProperties);
            quartzLTSConfig.setTaskTrackerProperties(taskTrackerProperties);

            QuartzLTSConfig.JobProperties jobProperties = PropertiesConfigurationFactory.createPropertiesConfiguration(QuartzLTSConfig.JobProperties.class, locations);
            quartzLTSConfig.setJobProperties(jobProperties);

            QuartzLTSProxyAgent agent = new QuartzLTSProxyAgent(quartzLTSConfig);
            QuartzProxyContext context = new QuartzProxyContext(quartzLTSConfig, agent);

            QuartzSchedulerBeanRegistrar registrar = new QuartzSchedulerBeanRegistrar(context);
            beanFactory.addPropertyEditorRegistrar(registrar);
        }
    }

    public void setLtsEnable(boolean ltsEnable) {
        this.ltsEnable = ltsEnable;
    }

    public void setLocations(String... locations) {
        this.locations = locations;
    }
}
