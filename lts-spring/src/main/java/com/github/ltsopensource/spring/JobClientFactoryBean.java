package com.github.ltsopensource.spring;

import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.support.JobCompletedHandler;
import java.util.Properties;
import lombok.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * JobClient Spring Bean 工厂类
 *
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@SuppressWarnings("rawtypes")
@Data
public class JobClientFactoryBean implements FactoryBean<JobClient>,
    InitializingBean, DisposableBean {

    private JobClient jobClient;
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
     * 额外参数配置
     */
    private Properties configs = new Properties();

    private boolean useRetryClient = true;

    /**
     * 任务完成处理接口
     */
    private JobCompletedHandler jobCompletedHandler;

    private String[] locations;

    @Override
    public JobClient getObject() throws Exception {
        return jobClient;
    }

    @Override
    public Class<?> getObjectType() {
        return JobClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 可以自己得到JobTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            jobClient.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        jobClient.stop();
    }
}
