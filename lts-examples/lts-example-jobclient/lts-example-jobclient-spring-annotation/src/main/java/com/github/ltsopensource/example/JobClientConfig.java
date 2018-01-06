package com.github.ltsopensource.example;

import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.spring.JobClientFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robert HG (254963746@qq.com) on 4/22/16.
 */
@Configuration
public class JobClientConfig {

    @Bean(name = "jobClient")
    public JobClient getJobClient() throws Exception {
        JobClientFactoryBean factoryBean = new JobClientFactoryBean();
        factoryBean.setLocations("lts.properties");
        factoryBean.afterPropertiesSet();
        factoryBean.start();
        return factoryBean.getObject();
    }
}
