package com.github.ltsopensource.spring.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.tasktracker")
public class TaskTrackerProperties extends com.github.ltsopensource.core.properties.TaskTrackerProperties {

}
