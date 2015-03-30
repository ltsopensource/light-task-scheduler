package com.lts.job.spring;

import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.util.Assert;
import com.lts.job.core.util.StringUtils;
import com.lts.job.task.tracker.TaskTracker;
import com.lts.job.task.tracker.runner.JobRunner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TaskTracker Spring Bean 工厂类
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class TaskTrackerFactoryBean implements FactoryBean<TaskTracker>, ApplicationContextAware, InitializingBean, DisposableBean {

    private ApplicationContext applicationContext;

    private TaskTracker taskTracker;

    private volatile boolean started;
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
    private String zookeeperAddress;
    /**
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String jobInfoSavePath;
    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 任务执行类
     */
    private Class jobRunnerClass;
    /**
     * master节点变化监听器
     */
    private MasterNodeChangeListener[] masterNodeChangeListeners;

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

    @Override
    public void destroy() throws Exception {
        if (started) {
            taskTracker.stop();
            started = false;
        }
    }

    public void checkProperties() {
        Assert.hasText(nodeGroup, "nodeGroup必须设值!");
        Assert.hasText(zookeeperAddress, "zookeeperAddress必须设值!");
        Assert.isTrue(workThreads > 0, "workThreads必须大于0!");
        Assert.notNull(jobRunnerClass, "jobRunnerClass不能为空");
        Assert.isAssignable(JobRunner.class, jobRunnerClass,
                StringUtils.format("jobRunnerClass必须实现{}接口!", JobRunner.class.getName()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        taskTracker = new TaskTracker();

        if (StringUtils.hasText(clusterName)) {
            taskTracker.setClusterName(clusterName);
        }
        taskTracker.setJobInfoSavePath(jobInfoSavePath);
        taskTracker.setWorkThreads(workThreads);
        taskTracker.setNodeGroup(nodeGroup);
        taskTracker.setZookeeperAddress(zookeeperAddress);
        taskTracker.setJobRunnerClass(jobRunnerClass);

        registerRunnerBeanDefinition();

        if (masterNodeChangeListeners != null) {
            for (MasterNodeChangeListener masterNodeChangeListener : masterNodeChangeListeners) {
                taskTracker.addMasterNodeChangeListener(masterNodeChangeListener);
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
        String beanName = jobRunnerClass.getName();
        if (!beanFactory.containsBean(beanName)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(jobRunnerClass);
            builder.setScope("prototype");
            beanFactory.registerBeanDefinition(beanName, builder.getBeanDefinition());
        }
    }

    public void start() {
        if (!started) {
            taskTracker.start();
            started = true;
        }
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public void setJobInfoSavePath(String jobInfoSavePath) {
        this.jobInfoSavePath = jobInfoSavePath;
    }

    public void setMasterNodeChangeListeners(MasterNodeChangeListener[] masterNodeChangeListeners) {
        this.masterNodeChangeListeners = masterNodeChangeListeners;
    }

    public void setJobRunnerClass(Class jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}