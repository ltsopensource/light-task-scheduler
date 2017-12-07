package com.github.ltsopensource.example.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.ltsopensource.example.handle.JobClientCompletedHandler;
import com.github.ltsopensource.example.handle.JobHandle;
import com.github.ltsopensource.example.handle.TaskTrackerJobRunner;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.spring.JobClientFactoryBean;
import com.github.ltsopensource.spring.JobTrackerFactoryBean;
import com.github.ltsopensource.spring.TaskTrackerAnnotationFactoryBean;
import com.github.ltsopensource.tasktracker.TaskTracker;

@Configuration
@ComponentScan(basePackages = "com.github.ltsopensource.example.handle")
public class LtsJobConfig implements ApplicationContextAware{
	private Logger logger = LoggerFactory.getLogger(DruidConfig.class);
	/*************************************************************************************/
	/***************************************JobCommon*************************************/
	/*************************************************************************************/
	@Value("${lts.cluster.name}")
    private String clusterName;
    @Value("${lts.job.fail.store}")
	private String failStore;
	@Value("${lts.zookeeper.registry-address}")
    private String registryAddress;
	/*************************************************************************************/
	/***************************************JobClient*************************************/
	/*************************************************************************************/
    @Value("${lts.jobclient.node-group}")
	private String nodeGroup;
	@Value("${lts.jobclient.use-retry-client}")
	private boolean useRetryClient;
	/*************************************************************************************/
	/***************************************JobTracker************************************/
	/*************************************************************************************/
	@Value("${lts.jobtracker.listen-port}")
	private int listenPort=35001;
	@Value("${lts.jobtracker.configs.job.logger}")
	private String jobLogger;
	@Value("${lts.jobtracker.configs.job.queue}")
	private String jobQueue;
	@Value("${lts.jobtracker.configs.jdbc.url}")
	private String url;
	@Value("${lts.jobtracker.configs.jdbc.username}")
	private String username;
	@Value("${lts.jobtracker.configs.jdbc.password}")
	private String password;
	@Value("${lts.jobtracker.configs.mongo.addresses}")
	private String addresses;
	@Value("${lts.jobtracker.configs.mongo.database}")
	private String database;
	@Value("${lts.jobtracker.configs.mongo.username}")
	private String musername;
	@Value("${lts.jobtracker.configs.mongo.password}")
	private String mpassword;
	/*************************************************************************************/
	/***************************************TaskTracker***********************************/
	/*************************************************************************************/
	@Value("${lts.tasktracker.node-group}")
	private String tnodeGroup;
	@Value("${lts.tasktracker.work-threads}")
	private int workThreads=64;
	/*************************************************************************************/
	/****************************************JobHandle************************************/
	/*************************************************************************************/
	@Autowired
	private JobClientCompletedHandler jobCompletedHandler;
//	@Autowired
//	private TaskTrackerJobRunner jobRunnerClass;
	
	private ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
    @SuppressWarnings("rawtypes")
	@Bean(name="jobClient",initMethod="start",destroyMethod = "destroy")
    public JobClient jobClient() throws Exception {
        logger.info("-----JobClient config init.-------");
        JobClientFactoryBean factory = new JobClientFactoryBean();
        factory.setClusterName(clusterName);
        factory.setRegistryAddress(registryAddress);
        factory.setNodeGroup(nodeGroup);
        factory.setUseRetryClient(useRetryClient);
        Properties configs = new Properties();
        configs.put("job.fail.store", failStore);
        factory.setConfigs(configs);
        factory.setJobCompletedHandler(jobCompletedHandler);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    @Bean(name="JobTracker",initMethod="start",destroyMethod = "destroy")
    public JobTracker jobTracker() throws Exception {
    	logger.info("-----JobTracker config init.-------");
    	JobTrackerFactoryBean factory = new JobTrackerFactoryBean();
    	factory.setClusterName(clusterName);
    	factory.setRegistryAddress(registryAddress);
    	factory.setListenPort(listenPort);
    	Properties configs = new Properties();
    	configs.put("job.logger", jobLogger);
    	configs.put("job.queue", jobQueue);
    	if(jobLogger.equalsIgnoreCase("mysql")||jobQueue.equalsIgnoreCase("mysql")){
    		configs.put("jdbc.url", url);
    		configs.put("jdbc.username", username);
    		configs.put("jdbc.password", password);
    	}
    	if(jobLogger.equalsIgnoreCase("mongo")||jobQueue.equalsIgnoreCase("mongo")){
    		configs.put("mongo.addresses", addresses);
    		configs.put("mongo.database", database);
    		configs.put("mongo.username", musername);
    		configs.put("mongo.password", mpassword);
    	}
    	factory.setConfigs(configs);
    	factory.afterPropertiesSet();
    	return factory.getObject();
    }
    @Bean(name="taskTracker",initMethod="start",destroyMethod = "destroy")
    public TaskTracker taskTracker() throws Exception {
    	logger.info("-----TaskTracker config init.-------");
    	TaskTrackerAnnotationFactoryBean factory = new TaskTrackerAnnotationFactoryBean();
    	factory.setClusterName(clusterName);
    	factory.setRegistryAddress(registryAddress);
    	factory.setNodeGroup(tnodeGroup);
    	factory.setWorkThreads(workThreads);
    	JobHandle.TASK_TRACKER_NODE_GROUP = tnodeGroup;
    	Properties configs = new Properties();
    	configs.put("job.fail.store", failStore);
    	factory.setConfigs(configs);
    	factory.setApplicationContext(applicationContext);
    	factory.setJobRunnerClass(TaskTrackerJobRunner.class);
    	factory.afterPropertiesSet();
    	return factory.getObject();
    }
}