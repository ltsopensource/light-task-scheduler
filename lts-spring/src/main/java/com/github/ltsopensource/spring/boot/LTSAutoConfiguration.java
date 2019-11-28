package com.github.ltsopensource.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import com.github.ltsopensource.spring.boot.annotation.EnableLTSScheduled;
import com.github.ltsopensource.spring.boot.properties.JobClientProperties;
import com.github.ltsopensource.spring.boot.properties.TaskTrackerProperties;
import com.github.ltsopensource.spring.quartz.QuartzLTSConfig.JobProperties;
import com.github.ltsopensource.spring.tasktracker.LTSScheduledBeanPostProcessor;

@Configuration
@ConditionalOnBean(annotation = EnableLTSScheduled.class)
@EnableConfigurationProperties({JobProperties.class, JobClientProperties.class, TaskTrackerProperties.class})
@AutoConfigureAfter({JobClientAutoConfiguration.class, JobTrackerAutoConfiguration.class, TaskTrackerAutoConfiguration.class})
public class LTSAutoConfiguration {
	@Autowired
	private TaskTrackerProperties taskTrackerProperties;
	@Autowired
	private JobClientProperties jobClientProperties;
	@Autowired
	private JobProperties jobProperties; 
	
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public LTSScheduledBeanPostProcessor scheduledAnnotationProcessor() {
		return new LTSScheduledBeanPostProcessor(jobClientProperties, jobProperties, taskTrackerProperties);
	}
}
