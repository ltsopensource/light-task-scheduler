package com.github.ltsopensource.spring.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.jobtracker")
public class JobTrackerProperties extends com.github.ltsopensource.core.properties.JobTrackerProperties {

    /**
     * 监听端口
     */
    private Integer listenPort;

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }
}
