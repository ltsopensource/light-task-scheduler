package com.lts.example.support;

import com.lts.core.listener.MasterChangeListener;
import com.lts.jobclient.JobClient;
import com.lts.jobtracker.JobTracker;
import com.lts.spring.JobClientFactoryBean;
import com.lts.spring.JobTrackerFactoryBean;
import com.lts.spring.TaskTrackerAnnotationFactoryBean;
import com.lts.tasktracker.TaskTracker;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 下面是给的参考示例，
 * 在其他Spring Bean 中就直接可以使用注解 @Autowired 注入使用了
 * 这里为了方便起见写在一起的，一般这三种节点是分开的，注意单独写
 *
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Configuration
public class LTSSpringConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(name = "jobClient")
    public JobClient getJobClient() throws Exception {
        JobClientFactoryBean factoryBean = new JobClientFactoryBean();
        factoryBean.setClusterName("test_cluster");
        factoryBean.setRegistryAddress("zookeeper://127.0.0.1:2181");
        factoryBean.setNodeGroup("test_jobClient");
        factoryBean.setMasterChangeListeners(new MasterChangeListener[]{
                new MasterChangeListenerImpl()
        });
        Properties configs = new Properties();
        configs.setProperty("job.fail.store", "leveldb");
        factoryBean.setConfigs(configs);

        factoryBean.afterPropertiesSet();
//        factoryBean.start();

        return factoryBean.getObject();
    }

    @Bean(name = "jobTracker")
    public JobTracker getJobTracker() throws Exception {
        JobTrackerFactoryBean factoryBean = new JobTrackerFactoryBean();
        factoryBean.setClusterName("test_cluster");
        factoryBean.setRegistryAddress("zookeeper://127.0.0.1:2181");
        factoryBean.setMasterChangeListeners(new MasterChangeListener[]{
                new MasterChangeListenerImpl()
        });
        Properties configs = new Properties();
        configs.setProperty("job.logger", "mongo");
        configs.setProperty("job.queue", "mongo");
        configs.setProperty("mongo.addresses", "127.0.0.1:27017");
        configs.setProperty("mongo.database", "lts");
        factoryBean.setConfigs(configs);

        factoryBean.afterPropertiesSet();
//        factoryBean.start();
        return factoryBean.getObject();
    }

    @Bean(name = "taskTracker")
    public TaskTracker getTaskTracker() throws Exception {
        TaskTrackerAnnotationFactoryBean factoryBean = new TaskTrackerAnnotationFactoryBean();
        factoryBean.setApplicationContext(applicationContext);
        factoryBean.setClusterName("test_cluster");
        factoryBean.setJobRunnerClass(SpringAnnotationJobRunner.class);
        factoryBean.setNodeGroup("test_trade_TaskTracker");
        factoryBean.setBizLoggerLevel("INFO");
        factoryBean.setRegistryAddress("zookeeper://127.0.0.1:2181");
        factoryBean.setMasterChangeListeners(new MasterChangeListener[]{
                new MasterChangeListenerImpl()
        });
        factoryBean.setWorkThreads(20);
        Properties configs = new Properties();
        configs.setProperty("job.fail.store", "leveldb");
        factoryBean.setConfigs(configs);

        factoryBean.afterPropertiesSet();
//        factoryBean.start();

        return factoryBean.getObject();
    }

}
