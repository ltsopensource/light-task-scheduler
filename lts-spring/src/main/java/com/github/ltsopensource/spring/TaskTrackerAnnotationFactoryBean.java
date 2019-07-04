package com.github.ltsopensource.spring;

import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.tasktracker.TaskTracker;
import java.util.Properties;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * TaskTracker Spring Bean 工厂类 如果用这个工厂类，那么JobRunner中引用SpringBean的话,只有通过注解的方式注入
 *
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@SuppressWarnings("rawtypes")
@Data
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
    private String dataPath;
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
     * 只有当使用 JobDispatcher 的时候才有效果
     */
    private String shardField;

    private String identity;

    private String bindIp;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();

    private String[] locations;

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

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {

    }

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
}
