package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.monitor.MonitorAgent;
import com.github.ltsopensource.spring.boot.annotation.EnableMonitor;
import com.github.ltsopensource.spring.boot.properties.MonitorProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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
        properties.checkProperties();

        agent = new MonitorAgent();

        agent.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtils.isNotEmpty(properties.getClusterName())) {
            agent.setClusterName(properties.getClusterName());
        }
        if (StringUtils.isNotEmpty(properties.getIdentity())) {
            agent.setIdentity(properties.getIdentity());
        }
        if (StringUtils.isNotEmpty(properties.getBindIp())) {
            agent.setBindIp(properties.getBindIp());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                agent.addConfig(entry.getKey(), entry.getValue());
            }
        }

        agent.start();
    }

    @Override
    public void destroy() throws Exception {
        if (agent != null) {
            agent.stop();
        }
    }

}
