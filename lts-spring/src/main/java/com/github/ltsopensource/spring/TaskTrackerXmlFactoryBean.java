package com.github.ltsopensource.spring;

import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.tasktracker.TaskTracker;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import java.util.Properties;
import lombok.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * TaskTracker Spring Bean 工厂类 如果用这个工厂类，那么JobRunner中引用SpringBean的话,只有通过xml的方式注入
 *
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@Data
public abstract class TaskTrackerXmlFactoryBean implements FactoryBean<TaskTracker>,
    InitializingBean, DisposableBean {

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

    private String identity;

    private String bindIp;

    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 业务日志级别
     */
    private Level bizLoggerLevel;
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

    @Override
    public void afterPropertiesSet() throws Exception {

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

    protected abstract JobRunner createJobRunner();

    @Override
    public void destroy() throws Exception {
        taskTracker.stop();
    }
}
