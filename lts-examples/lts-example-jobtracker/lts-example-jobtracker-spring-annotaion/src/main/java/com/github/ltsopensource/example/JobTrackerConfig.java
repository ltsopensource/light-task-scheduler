package com.github.ltsopensource.example;

import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.spring.JobTrackerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robert HG (254963746@qq.com) on 4/22/16.
 */
@Configuration
public class JobTrackerConfig {

    @Bean(name = "jobTracker")
    public JobTracker getJobTracker() throws Exception {
        JobTrackerFactoryBean factoryBean = new JobTrackerFactoryBean();
        factoryBean.setLocations("lts.properties");
        factoryBean.afterPropertiesSet();
        factoryBean.start();
        return factoryBean.getObject();
    }

}
