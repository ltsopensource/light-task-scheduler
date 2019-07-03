# LTS User Documentation

LTS (light-task-scheduler) is mainly used to solve distributed task scheduling problems, supporting real-time tasks, timing tasks and Cron tasks. It has good scalability, scalability, robustness and stability and is used by many companies. It also hopes that open source enthusiasts will contribute together.

## ---> There are recruiters at the bottom

## project address
Github address:
[https://github.com/ltsopensource/light-task-scheduler](https://github.com/ltsopensource/light-task-scheduler)

Oschina address:

[http://git.oschina.net/hugui/light-task-scheduler](http://git.oschina.net/hugui/light-task-scheduler)

example:

[https://github.com/ltsopensource/lts-examples](https://github.com/ltsopensource/lts-examples)

Document address (in the process of being updated, whichever is later):

[https://www.gitbook.com/book/qq254963746/lts/details](https://www.gitbook.com/book/qq254963746/lts/details)

Both addresses will be updated synchronously. Interested, please add QQ group: 109500214 (plus group password: hello world) to explore and improve together. The more people support, the more motivated to update, like to remember the star in the upper right corner.

##1.7.2-SNAPSHOT (master) change main point
1. Optimize the BizLogger in the JobContext, remove the threadlocal from the original, solve the problem of taskTracker multithreading, remove the LtsLoggerFactory.getLogger() usage

## Frame Overview
LTS has the following four main nodes:

* `JobClient`: Mainly responsible for submitting tasks and receiving feedback on task execution feedback.
* `JobTracker`: responsible for receiving and assigning tasks, task scheduling.
* `TaskTracker`: Responsible for executing tasks and executing feedback to JobTracker.
* `LTS-Admin`: (Management Background) is mainly responsible for node management, task queue management, monitoring management, etc.

The `JobClient`, `JobTracker`, and `TaskTracker` nodes are all `stateless`.
Multiple and dynamic deletions can be deployed to achieve load balancing and achieve greater load capacity, and the framework uses FailStore strategy to make LTS fault tolerant.

The LTS registry provides multiple implementations (Zookeeper, redis, etc.), the registry performs node information exposure, and the master election. (Mongo or Mysql) stores task queues and task execution logs, netty or mina for underlying communication, and provides a variety of serialization methods fastjson, hessian2, java, etc.

LTS supports task types:

* Real-time tasks: Submit tasks that will be executed immediately after they are submitted.
* Scheduled tasks: Tasks performed at a specified point in time, such as today at 3 o'clock (single).
* Cron task: CronExpression, similar to quartz (but not using quartz) such as 0 0/1 * * * ?

Support dynamic modification of task parameters, task execution time and other settings, support dynamic add tasks in the background, support Cron task pause, support manual stop tasks (conditional), support task monitoring statistics, support task execution monitoring of each node, JVM Monitoring and so on.

## Architecture

![LTS architecture](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS_architecture.png?dir=0&filepath=docs%2FLTS_architecture.png&oid=262a5234534e2d9fa8862f3e632c5551ebd95e21&sha=d01be5d59e8d768f49bbdc66c8334c37af8f7af5)

## Concept Description
 
 ###node group
 1. English name NodeGroup, a node group is equivalent to a small cluster, each node in the same node group is peer-to-peer, equivalent, providing the same service to the outside.
 2. Each node group has a master node. This master node is dynamically selected by the LTS. When a master node hangs, the LTS will immediately select another master node. The framework provides an API listening interface to the user.
 
 ###FailStore
 1. As the name implies, this is mainly used for failed storage. It is mainly used for node fault tolerance. When the remote data interaction fails, it is stored locally and waits for the remote communication to recover, and then the data is submitted.
 2. FailStore main user JobClient task submission, TaskTracker task feedback, TaskTracker business log transmission scenario.
 3. FailStore currently provides several implementations: leveldb, rocksdb, berkeleydb, mapdb, ltsdb, which can be used freely, and users can also use SPI extensions to implement their own implementations.
 
 
 ## Flowchart
 The figure below is a standard real-time task execution flow.

![LTS progress](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS_progress.png?dir=0&filepath=docs%2FLTS_progress.png&oid=22f60a83b51b26bac8dabbb5053ec9913cefc45c&sha=774aa73d186470aedbb8f4da3c04a86a6022be05)
## LTS-Admin new interface preview

![LTS Admin](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS-Admin/LTS-Admin-cron-job-queue.png?dir=0&filepath=docs%2FLTS-Admin%2FLTS-Admin-cron-job-queue.png&oid=aecaf01bca5270a53b144891baaa3d7e56d47706&sha=a4fd9f31df9e1fc6d389a16bdc8d1964bb854766)
Currently the background comes with a simple authentication function provided by [ztajy] (https://github.com/ztajy). The username and password are in auth.cfg and are modified by the user.

##Characteristic
###1, Spring support
LTS can completely eliminate the Spring framework, but considering that the user framework uses the Spring framework, LTS also provides support for Spring, including Xml and annotations, and introduces `lts-spring.jar`.
###2, business logger
A service logger is provided on the TaskTracker side, and the application program is used. Through the service logger, the service logs can be submitted to the JobTracker. These service logs can be connected in series through the task ID, and the execution of the task can be viewed in real time in the LTS-Admin. schedule.
###3, SPI extension support
SPI extension can achieve zero intrusion, only need to implement the corresponding interface, and can be used by LTS. The open interface that is currently open has

1. For the extension of the task queue, the user can choose not to use mysql or mongo as the queue storage, or you can implement it yourself.
2. For the extension of the business logger, currently mainly supports console, mysql, mongo, users can also choose to transfer logs to other places through extension.

###4,Failover
After the TaskTracker is performing the task, the JobTracker will immediately assign all the tasks assigned to the TaskTracker of the downtime to other normal TaskTracker nodes for execution.
###5, node monitoring
You can perform resource monitoring, task monitoring, etc. on the JobTracker and TaskTracker nodes, and you can view them in the LTS-Admin management background in real time, and then perform reasonable resource allocation.
###6, Diversified task execution result support
The LTS framework provides four implementation results support, `EXECUTE_SUCCESS`, `EXECUTE_FAILED`, `EXECUTE_LATER`, `EXECUTE_EXCEPTION`, and a corresponding processing mechanism for each result, such as retry.

* EXECUTE_SUCCESS: The execution is successful. In this case, the client is directly fed back (if the task is set to be fed back to the client).
* EXECUTE_FAILED: Execution failed. In this case, it is directly fed back to the client without retrying.
* EXECUTE_LATER: Execute later (retry required). In this case, the client is not fed back. The retry strategy adopts the strategy of 1min, 2min, 3min. The default maximum retry number is 10 times. The user can modify this weight by parameter setting. The number of trials.
* EXECUTE_EXCEPTION: Execution exception, this situation will also be retried (retry strategy, ibid.)

###7,FailStore fault tolerance
Using the FailStore mechanism for node fault tolerance, Fail And Store does not affect the operation of the current application due to the instability of remote communication. For details of the FailStore, please refer to the FailStore description in the concept description.

##Project Compilation Packaging
The project is mainly built using maven, and currently provides shell script packaging.
Environment dependency: `Java(jdk1.6+)` `Maven`

User usage is generally divided into two types:
###1, Maven build
The lts jar package can be uploaded to the local repository via the maven command. Add the corresponding repository in the parent pom.xml and upload it with the deploy command. For specific reference, you can refer to the example in lts.
###2, direct Jar reference
It is necessary to package the individual modules of lts into separate jar packages and introduce all lts dependency packages. Specific reference to which jar package can refer to the example in lts.

##JobTracker and LTS-Admin deployment
Provide `(cmd)windows` and `(shell)linux` scripts for compiling and deploying:

1. Run the `sh build.sh` or `build.cmd` script in the root directory to generate the `lts-{version}-bin` folder in the `dist` directory.

2. The following is the directory structure, where the bin directory is mainly the startup script for JobTracker and LTS-Admin. `jobtracker` is the configuration file of JobTracker and the jar package that needs to be used. `lts-admin` is the war package and configuration file related to LTS-Admin.
Lts-{version}-bin file structure

```
-- lts-${version}-bin
    |-- bin
    | |-- jobtracker.cmd
    | |-- jobtracker.sh
    | |-- lts-admin.cmd
    | |-- lts-admin.sh
    | |-- lts-monitor.cmd
    | |-- lts-monitor.sh
    | |-- tasktracker.sh
    |-- conf
    | |-- log4j.properties
    | |-- lts-admin.cfg
    | |-- lts-monitor.cfg
    | |-- readme.txt
    | |-- tasktracker.cfg
    | |-- zoo
    | |-- jobtracker.cfg
    | |-- log4j.properties
    | |-- lts-monitor.cfg
    |-- lib
    | |-- *.jar
    |-- war
        |-- jetty
        | |-- lib
        | |-- *.jar
        |-- lts-admin.war

```
        
3. JobTracker starts. If you want to start a node, directly modify the configuration file under `conf/zoo`, then run `sh jobtracker.sh zoo start`. If you want to start two JobTracker nodes, then you need to copy a zoo. For example, named `zoo2`, modify the configuration file under `zoo2`, and then run `sh jobtracker.sh zoo2 start`. The `jobtracker-zoo.out` log is generated under the logs folder.
4. LTS-Admin starts. Modify the configuration under `conf/lts-monitor.cfg` and `conf/lts-admin.cfg`, then run `sh lts-admin.sh` or `lts- under `bin`. The admin.cmd` script is fine. The `lts-admin.out` log will be generated in the logs folder. After the startup succeeds, the access address will be printed in the log. The user can access it through this access address.

##JobClient(Deployment)Use
The jars that need to introduce lts are `lts-jobclient-{version}.jar`, `lts-core-{version}.jar` and other third-party dependencies.
###API mode startup
```java
JobClient jobClient = new RetryJobClient();
jobClient.setNodeGroup("test_jobClient");
jobClient.setClusterName("test_cluster");
jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
jobClient.start();

// Submit the task
Job job = new Job();
job.setTaskId("3213213123");
job.setParam("shopId", "11111");
job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
// job.setCronExpression("0 0/1 * * * ?"); // Support for cronExpression expressions
// job.setTriggerTime(new Date()); // Support for specified time execution
Response response = jobClient.submitJob(job);
```
    
###Spring XML mode startup
```xml
<bean id="jobClient" class="com.github.ltsopensource.spring.JobClientFactoryBean">
    <property name="clusterName" value="test_cluster"/>
    <property name="registryAddress" value="zookeeper://127.0.0.1:2181"/>
    <property name="nodeGroup" value="test_jobClient"/>
    <property name="masterChangeListeners">
        <list>
            <bean class="com.github.ltsopensource.example.support.MasterChangeListenerImpl"/>
        </list>
    </property>
    <property name="jobFinishedHandler">
        <bean class="com.github.ltsopensource.example.support.JobFinishedHandlerImpl"/>
    </property>
    <property name="configs">
        <props>
            <!-- Parameters -->
            <prop key="job.fail.store">leveldb</prop>
        </props>
    </property>
</bean>
```
###Spring Full annotation method
```java
@Configuration
public class LTSSpringConfig {

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
        return factoryBean.getObject();
    }
}
```

##TaskTracker (deployment use)
The jars that need to introduce lts are `lts-tasktracker-{version}.jar`, `lts-core-{version}.jar` and other third-party dependencies.
###Defining your own task execution class
```java
public class MyJobRunner implements JobRunner {
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        try {
            // TODO 业务逻辑
            // 会发送到 LTS (JobTracker上)
            jobContext.getBizLogger().info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }
}
```
###API mode startup
```java
TaskTracker taskTracker = new TaskTracker();
taskTracker.setJobRunnerClass(MyJobRunner.class);
taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
taskTracker.setNodeGroup("test_trade_TaskTracker");
taskTracker.setClusterName("test_cluster");
taskTracker.setWorkThreads(20);
taskTracker.start();
```
###Spring XML mode startup
```xml
<bean id="taskTracker" class="com.github.ltsopensource.spring.TaskTrackerAnnotationFactoryBean" init-method="start">
    <property name="jobRunnerClass" value="com.github.ltsopensource.example.support.MyJobRunner"/>
    <property name="bizLoggerLevel" value="INFO"/>
    <property name="clusterName" value="test_cluster"/>
    <property name="registryAddress" value="zookeeper://127.0.0.1:2181"/>
    <property name="nodeGroup" value="test_trade_TaskTracker"/>
    <property name="workThreads" value="20"/>
    <property name="masterChangeListeners">
        <list>
            <bean class="com.github.ltsopensource.example.support.MasterChangeListenerImpl"/>
        </list>
    </property>
    <property name="configs">
        <props>
            <prop key="job.fail.store">leveldb</prop>
        </props>
    </property>
</bean>
```
###Spring annotation mode startup
```java
@Configuration
public class LTSSpringConfig implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    @Bean(name = "taskTracker")
    public TaskTracker getTaskTracker() throws Exception {
        TaskTrackerAnnotationFactoryBean factoryBean = new TaskTrackerAnnotationFactoryBean();
        factoryBean.setApplicationContext(applicationContext);
        factoryBean.setClusterName("test_cluster");
        factoryBean.setJobRunnerClass(MyJobRunner.class);
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
//      factoryBean.start();
        return factoryBean.getObject();
    }
}
```
##Parameter Description
[Parameter Description] (https://qq254963746.gitbooks.io/lts/content/use/config-name.html)

##Recommendations
Generally, only one JobClient instance is needed in a JVM. Do not create a new JobClient instance for each task. This will waste a lot of resources, because a JobClient can submit multiple tasks. The same JVM generally tries to keep only one instance of TaskTracker. If it is too much, it may cause waste of resources. When encountering a TaskTracker to run multiple tasks, please refer to "A TaskTracker performs multiple tasks" below.
##TaskTracker performs multiple tasks
Sometimes, the business scenario needs to perform a variety of tasks, and some people will ask if it is necessary to have a TaskTracker to execute each task type. My answer is no. If you are in a JVM, it is best to use a TaskTracker to run multiple tasks, because using multiple TaskTracker instances in a JVM is a waste of resources (of course, when you have a certain amount of tasks, you can This task is performed using a TaskTracker node alone). So how can I implement a TaskTracker to perform multiple tasks? Below is a reference example I gave.

```java
/**
 * 总入口，在 taskTracker.setJobRunnerClass(JobRunnerDispatcher.class)
 * JobClient 提交 任务时指定 Job 类型  job.setParam("type", "aType")
 */
public class JobRunnerDispatcher implements JobRunner {

    private static final ConcurrentHashMap<String/*type*/, JobRunner>
            JOB_RUNNER_MAP = new ConcurrentHashMap<String, JobRunner>();

    static {
        JOB_RUNNER_MAP.put("aType", new JobRunnerA()); // 也可以从Spring中拿
        JOB_RUNNER_MAP.put("bType", new JobRunnerB());
    }

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        Job job = jobContext.getJob();
        String type = job.getParam("type");
        return JOB_RUNNER_MAP.get(type).run(job);
    }
}

class JobRunnerA implements JobRunner {
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        //  TODO A类型Job的逻辑
        return null;
    }
}

class JobRunnerB implements JobRunner {
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        // TODO B类型Job的逻辑
        return null;
    }
}
```
##TaskTracker's JobRunner test
Generally, when writing TaskTracker, you only need to test whether JobRunner's implementation logic is correct, and you don't want to start LTS for remote testing. To facilitate testing, LTS provides a quick test method for JobRunner. Your own test class integrates `com.github.ltsopensource.tasktracker.runner.JobRunnerTester` and implements the `initContext` and `newJobRunner` methods. For example, in [lts-examples] (https://github.com/ltsopensource/lts-examples):

```java
public class TestJobRunnerTester extends JobRunnerTester {

    public static void main(String[] args) throws Throwable {
        //  Mock Job 数据
        Job job = new Job();
        job.setTaskId("2313213");

        JobContext jobContext = new JobContext();
        jobContext.setJob(job);

        JobExtInfo jobExtInfo = new JobExtInfo();
        jobExtInfo.setRetry(false);

        jobContext.setJobExtInfo(jobExtInfo);

        // 运行测试
        TestJobRunnerTester tester = new TestJobRunnerTester();
        Result result = tester.run(jobContext);
        System.out.println(JSON.toJSONString(result));
    }

    @Override
    protected void initContext() {
        // TODO 初始化Spring容器
    }

    @Override
    protected JobRunner newJobRunner() {
        return new TestJobRunner();
    }
}
```

##Spring Quartz Cron task seamless access
For Quartz's Cron task, you only need to add some code in the Spring configuration to access the LTS platform.

```xml
<bean class="com.github.ltsopensource.spring.quartz.QuartzLTSProxyBean">
    <property name="clusterName" value="test_cluster"/>
    <property name="registryAddress" value="zookeeper://127.0.0.1:2181"/>
    <property name="nodeGroup" value="quartz_test_group"/>
</bean>
```
##Spring Boot Support

```java
@SpringBootApplication
@EnableJobTracker       // 启动JobTracker
@EnableJobClient        // 启动JobClient
@EnableTaskTracker      // 启动TaskTracker
@EnableMonitor          // 启动Monitor
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

All that's left is to add the appropriate configuration in application.properties. See the example under the `com.github.ltsopensource.examples.springboot` package in lts-example.


##Multiple network card selection problem
When the machine has two network cards in the internal network, sometimes, the user wants the LTS traffic to go to the external network card, then in the host, the mapping address of the host name is changed to the external network card address, and the intranet is the same. .

##About node identification problem
If the node identifier is set when the node is started, the LTS will set a UUID as the node identifier by default. The readability will be poor, but the uniqueness of each node can be guaranteed. If the user can guarantee the uniqueness of the node identifier, he can pass `setIdentity` to set, for example, if each node is deployed on a machine (a virtual machine), then identity can be set to the host name

##SPIExtension Description
Support SPI extensions for JobLogger, JobQueue, etc.

##[Compare with other solutions](https://qq254963746.gitbooks.io/lts/content/introduce/compareother.html)


##LTS-AdminStarts with jetty (default), hangs the solution from time to time
See [issue#389] (https://github.com/ltsopensource/light-task-scheduler/issues/389)

# hiring! ! !
Working years, more than three years

Academic qualifications

Expectation level P6 (senior Java engineer) / P7 (technical expert)

Job description  

The member platform is responsible for the user system of Alibaba Group, supports the user needs of each line of business lines within the group, and supports the user communication and business communication of the Group's external cooperation.
Including user login & authorization, session system, registration, account management, account security and other functions at the end, the underlying user information service, session and credential management, etc., is one of the Group's core product lines, carrying hundreds of billions of calls per day. Volume, peak tens of millions of QPS, and distributed global cloud architecture and so on.

As a software engineer, you will work on our core products that provide key functions for our commercial infrastructure.
Depending on your interests and experience, you can work in one or more of the following areas: globalization, user experience, data security, machine learning, system high availability, and more.

1. Independently complete the system analysis and design of small and medium-sized projects, and lead the tasks of detailed design and coding to ensure the progress and quality of the project;
2. Be able to complete the code review task in the team, ensure the validity and correctness of the relevant code, and be able to provide relevant performance and stability recommendations through code review;
3. Participate in the construction of a versatile, flexible and intelligent business support platform to support complex services in multiple scenarios at the upper level.
job requirements  
4. Solid foundation in Java programming, familiar with common Java open source frameworks;
5. Have practical experience in developing high-performance, high-availability data applications based on database, cache, and 
distributed storage, and master the LINUX operating system;
6. Have good ability to identify and design common frameworks and modules;
7. Love technology, work conscientiously and rigorously, have a near-demanding awareness of system quality, and be good at communication 
and teamwork;
8. Experience in large-scale e-commerce website or core system development and design work in the financial industry is preferred;
9. Experience in working with big data processing, algorithms, and machine learning is preferred.
Interested, you can send your resume to hugui.hg@alibaba-inc.com Welcome delivery

