package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.monitor.MonitorAgent;
import com.github.ltsopensource.spring.boot.annotation.EnableMonitor;
import com.github.ltsopensource.spring.boot.properties.MonitorProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableMonitor.class)
@EnableConfigurationProperties(MonitorProperties.class)
public class MonitorAutoConfiguration implements InitializingBean, DisposableBean {

    @Autowired(required = false)
    private MonitorProperties properties;

    private MonitorAgent agent;

    @Override
    public void afterPropertiesSet() throws Exception {

        agent.start();
    }

    @Override
    public void destroy() throws Exception {
        if (agent != null) {
            agent.stop();
        }
    }

}
