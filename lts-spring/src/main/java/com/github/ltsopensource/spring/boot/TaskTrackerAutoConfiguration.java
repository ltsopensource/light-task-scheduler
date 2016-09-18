package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.AbstractJobNode;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.spring.boot.annotation.EnableTaskTracker;
import com.github.ltsopensource.spring.boot.annotation.JobRunner4TaskTracker;
import com.github.ltsopensource.spring.boot.properties.TaskTrackerProperties;
import com.github.ltsopensource.spring.tasktracker.JobDispatcher;
import com.github.ltsopensource.spring.tasktracker.JobRunnerHolder;
import com.github.ltsopensource.spring.tasktracker.LTS;
import com.github.ltsopensource.tasktracker.TaskTracker;
import com.github.ltsopensource.tasktracker.TaskTrackerBuilder;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.github.ltsopensource.tasktracker.runner.RunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableTaskTracker.class)
@EnableConfigurationProperties(TaskTrackerProperties.class)
public class TaskTrackerAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private TaskTrackerProperties properties;
    private TaskTracker taskTracker;

    @Override
    protected void initJobNode() {

        taskTracker = TaskTrackerBuilder.buildByProperties(properties);

        if (!isEnableDispatchRunner()) {

            Map<String, Object> jobRunners = applicationContext.getBeansWithAnnotation(JobRunner4TaskTracker.class);
            if (CollectionUtils.isNotEmpty(jobRunners)) {
                if (jobRunners.size() > 1) {
                    throw new IllegalArgumentException("annotation @" + JobRunner4TaskTracker.class.getSimpleName() + " only should have one");
                }
                for (final Map.Entry<String, Object> entry : jobRunners.entrySet()) {
                    Object handler = entry.getValue();
                    if (handler instanceof JobRunner) {
                        taskTracker.setRunnerFactory(new RunnerFactory() {
                            @Override
                            public JobRunner newRunner() {
                                return (JobRunner) entry.getValue();
                            }
                        });
                    } else {
                        throw new IllegalArgumentException(entry.getKey() + "  is not instance of " + JobRunner.class.getName());
                    }
                }
            }
        } else {

            Map<String, Object> ltsBeanMap = applicationContext.getBeansWithAnnotation(LTS.class);
            if (CollectionUtils.isNotEmpty(ltsBeanMap)) {
                for (Map.Entry<String, Object> entry : ltsBeanMap.entrySet()) {
                    Object bean = entry.getValue();
                    JobRunnerHolder.addLTSBean(bean);
                }
            }
            registerRunnerBeanDefinition();
            taskTracker.setRunnerFactory(new RunnerFactory() {
                @Override
                public JobRunner newRunner() {
                    return (JobRunner) applicationContext.getBean(JOB_RUNNER_BEAN_NAME);
                }
            });
        }

    }

    String JOB_RUNNER_BEAN_NAME = "LTS_".concat(JobDispatcher.class.getSimpleName());

    private void registerRunnerBeanDefinition() {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        if (!beanFactory.containsBean(JOB_RUNNER_BEAN_NAME)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(JobDispatcher.class);
            builder.setScope(BeanDefinition.SCOPE_SINGLETON);
            builder.setLazyInit(false);
            builder.getBeanDefinition().getPropertyValues().addPropertyValue("shardField", properties.getDispatchRunner().getShardValue());
            beanFactory.registerBeanDefinition(JOB_RUNNER_BEAN_NAME, builder.getBeanDefinition());
        }
    }

    private boolean isEnableDispatchRunner() {
        return properties.getDispatchRunner() != null && properties.getDispatchRunner().isEnable();
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.TASK_TRACKER;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return taskTracker;
    }
}
