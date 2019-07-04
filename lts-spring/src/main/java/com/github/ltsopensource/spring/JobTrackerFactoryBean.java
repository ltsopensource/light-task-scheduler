package com.github.ltsopensource.spring;

import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.jobtracker.support.OldDataHandler;
import java.util.Properties;
import lombok.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * JobTracker Spring Bean 工厂类
 *
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@Data
public class JobTrackerFactoryBean implements FactoryBean<JobTracker>,
        InitializingBean, DisposableBean {

    private JobTracker jobTracker;
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();
    /**
     * 监听端口
     */
    private Integer listenPort;

    private String identity;

    private String bindIp;
    /**
     * 老数据处理接口
     */
    private OldDataHandler oldDataHandler;

    private String[] locations;

    @Override
    public JobTracker getObject() throws Exception {
        return jobTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return JobTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * 可以自己得到JobTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            jobTracker.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        jobTracker.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
