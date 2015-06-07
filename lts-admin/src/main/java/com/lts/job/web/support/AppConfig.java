package com.lts.job.web.support;

import com.lts.job.web.cluster.AdminApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by hugui on 6/6/15.
 */
@Configuration
public class AppConfig {

    @Bean(name = "application")
    public AdminApplication getAdminApplication() throws Exception {
        AdminAppFactoryBean factoryBean = new AdminAppFactoryBean();
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

}
