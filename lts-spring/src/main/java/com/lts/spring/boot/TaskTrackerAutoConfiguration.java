package com.lts.spring.boot;

import com.lts.core.cluster.AbstractJobNode;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.spring.boot.annotation.EnableTaskTracker;
import com.lts.spring.boot.annotation.JobRunner4TaskTracker;
import com.lts.spring.boot.properties.TaskTrackerProperties;
import com.lts.spring.tasktracker.JobDispatcher;
import com.lts.spring.tasktracker.JobRunnerHolder;
import com.lts.spring.tasktracker.LTS;
import com.lts.tasktracker.TaskTracker;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
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
        taskTracker = new TaskTracker();
        taskTracker.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtils.isNotEmpty(properties.getClusterName())) {
            taskTracker.setClusterName(properties.getClusterName());
        }
        if (StringUtils.isNotEmpty(properties.getIdentity())) {
            taskTracker.setIdentity(properties.getIdentity());
        }
        if (StringUtils.isNotEmpty(properties.getNodeGroup())) {
            taskTracker.setNodeGroup(properties.getNodeGroup());
        }
        if (StringUtils.isNotEmpty(properties.getDataPath())) {
            taskTracker.setDataPath(properties.getDataPath());
        }
        if (StringUtils.isNotEmpty(properties.getBindIp())) {
            taskTracker.setBindIp(properties.getBindIp());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                taskTracker.addConfig(entry.getKey(), entry.getValue());
            }
        }
        if (properties.getWorkThreads() != 0) {
            taskTracker.setWorkThreads(properties.getWorkThreads());
        }

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
