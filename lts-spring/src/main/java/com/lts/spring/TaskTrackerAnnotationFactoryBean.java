package com.lts.spring;

import com.lts.core.commons.utils.Assert;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Level;
import com.lts.core.listener.MasterChangeListener;
import com.lts.tasktracker.TaskTracker;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Properties;

/**
 * TaskTracker Spring Bean 工厂类
 * 如果用这个工厂类，那么JobRunner中引用SpringBean的话,只有通过注解的方式注入
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
public class TaskTrackerAnnotationFactoryBean implements FactoryBean<TaskTracker>, ApplicationContextAware,
        InitializingBean, DisposableBean {

    private ApplicationContext applicationContext;
    private TaskTracker taskTracker;
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 节点组名称
     */
    private String nodeGroup;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String failStorePath;
    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 任务执行类
     */
    private Class jobRunnerClass;
    /**
     * 业务日志级别
     */
    private Level bizLoggerLevel;
    /**
     * spring中jobRunner的bean name
     */
    private String jobRunnerBeanName;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();

    @Override
    public TaskTracker getObject() throws Exception {
        return taskTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    public void checkProperties() {
        Assert.hasText(clusterName, "clusterName must have value.");
        Assert.hasText(nodeGroup, "nodeGroup must have value.");
        Assert.hasText(registryAddress, "registryAddress must have value.");
        Assert.isTrue(workThreads > 0, "workThreads must > 0.");
        Assert.notNull(jobRunnerClass, "jobRunnerClass must have value");
        Assert.isAssignable(JobRunner.class, jobRunnerClass,
                StringUtils.format("jobRunnerClass should be implements {}.", JobRunner.class.getName()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        taskTracker = new TaskTracker();

        taskTracker.setClusterName(clusterName);
        taskTracker.setFailStorePath(failStorePath);
        taskTracker.setWorkThreads(workThreads);
        taskTracker.setNodeGroup(nodeGroup);
        taskTracker.setRegistryAddress(registryAddress);
        taskTracker.setJobRunnerClass(jobRunnerClass);

        if (bizLoggerLevel != null) {
            taskTracker.setBizLoggerLevel(bizLoggerLevel);
        }

        registerRunnerBeanDefinition();

        // 设置config
        for (Map.Entry<Object, Object> entry : configs.entrySet()) {
            taskTracker.addConfig(entry.getKey().toString(), entry.getValue().toString());
        }

        taskTracker.setRunnerFactory(new RunnerFactory() {
            @Override
            public JobRunner newRunner() {
                return (JobRunner) applicationContext.getBean(jobRunnerBeanName);
            }
        });

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                taskTracker.addMasterChangeListener(masterChangeListener);
            }
        }

    }

    /**
     * 将 JobRunner 生成Bean放入spring容器中管理
     * 采用原型 scope， 所以可以在JobRunner中使用@Autowired
     */
    private void registerRunnerBeanDefinition() {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        jobRunnerBeanName = "LTS_".concat(jobRunnerClass.getName());
        if (!beanFactory.containsBean(jobRunnerBeanName)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(jobRunnerClass);
            builder.setScope("prototype");
            beanFactory.registerBeanDefinition(jobRunnerBeanName, builder.getBeanDefinition());
        }
    }

    /**
     * 可以自己得到TaskTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            taskTracker.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        taskTracker.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setFailStorePath(String failStorePath) {
        this.failStorePath = failStorePath;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public void setJobRunnerClass(Class jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public void setMasterChangeListeners(MasterChangeListener[] masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setBizLoggerLevel(String bizLoggerLevel) {
        if (StringUtils.isNotEmpty(bizLoggerLevel)) {
            this.bizLoggerLevel = Level.valueOf(bizLoggerLevel);
        }
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }
}
