package com.lts.admin.support.spring;

import com.lts.admin.cluster.AdminAppContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@Configuration
public class AppConfig {

    @Bean(name = "appContext")
    public AdminAppContext getAdminAppContext() throws Exception {
        AdminAppFactoryBean factoryBean = new AdminAppFactoryBean();
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

}
