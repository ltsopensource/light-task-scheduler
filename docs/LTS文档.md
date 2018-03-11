# LTS用户文档
	
LTS(light-task-scheduler)主要用于解决分布式任务调度问题，支持实时任务，定时任务和Cron任务。有较好的伸缩性，扩展性，健壮稳定性而被多家公司使用，同时也希望开源爱好者一起贡献。

## 项目地址
github地址:
[https://github.com/ltsopensource/light-task-scheduler](https://github.com/ltsopensource/light-task-scheduler)

oschina地址:
[http://git.oschina.net/hugui/light-task-scheduler](http://git.oschina.net/hugui/light-task-scheduler)

这两个地址都会同步更新。感兴趣，请加QQ群：109500214 一起探讨、完善。越多人支持，就越有动力去更新，喜欢记得右上角star哈。

## 框架概况
LTS 有主要有以下四种节点：

* JobClient：主要负责提交任务, 并接收任务执行反馈结果。
* JobTracker：负责接收并分配任务，任务调度。
* TaskTracker：负责执行任务，执行完反馈给JobTracker。
* LTS-Admin：（管理后台）主要负责节点管理，任务队列管理，监控管理等。

其中JobClinet，JobTracker，TaskTracker节点都是`无状态`的。
可以部署多个并动态的进行删减，来实现负载均衡，实现更大的负载量, 并且框架采用FailStore策略使LTS具有很好的容错能力。 

LTS注册中心提供多种实现（Zookeeper，redis等），注册中心进行节点信息暴露，master选举。(Mongo or Mysql)存储任务队列和任务执行日志, netty做底层通信。

LTS支持任务类型：

* 实时任务：提交了之后立即就要执行的任务。
* 定时任务：在指定时间点执行的任务，譬如 今天3点执行（单次）。
* Cron任务：CronExpression，和quartz类似（但是不是使用quartz实现的）譬如 0 0/1 * * * ?

## 架构图

![LTS architecture](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS_architecture.png?dir=0&filepath=docs%2FLTS_architecture.png&oid=1e5daa62b8d032daaa47eab4a84ab1d4c8962c33&sha=774aa73d186470aedbb8f4da3c04a86a6022be05)

## 概念说明

###节点组
1. 英文名称 NodeGroup,一个节点组等同于一个小的集群，同一个节点组中的各个节点是对等的，等效的，对外提供相同的服务。
2. 没个节点组中都有一个master节点，这个master节点是由LTS动态选出来的，当一个master节点挂掉之后，LTS会立马选出另外一个master节点，框架提供API监听接口给用户。

###FailStore
1. 顾名思义，这个主要是用于失败了存储的，主要用于节点容错，当远程数据交互失败之后，存储在本地，等待远程通信恢复的时候，再将数据提交。
2. FailStore主要用户JobClient的任务提交，TaskTracker的任务反馈，TaskTracker的业务日志传输的场景下。
3. FailStore目前提供三种实现：leveldb，rocksdb，berkeleydb，用于可以自由选择使用哪种。


## 流程图
下图是一个标准的实时任务执行流程。

![LTS progress](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS_progress.png?dir=0&filepath=docs%2FLTS_progress.png&oid=22f60a83b51b26bac8dabbb5053ec9913cefc45c&sha=774aa73d186470aedbb8f4da3c04a86a6022be05)

##特性
###1、Spring支持
LTS可以完全不用Spring框架，但是考虑到很用用户项目中都是用了Spring框架，所以LTS也提供了对Spring的支持，包括Xml和注解，引入`lts-spring.jar`即可。
###2、业务日志记录器
在TaskTracker端提供了业务日志记录器，供应用程序使用，通过这个业务日志器，可以将业务日志提交到JobTracker，这些业务日志可以通过任务ID串联起来，可以在LTS-Admin中实时查看任务的执行进度。
###3、SPI扩展支持
SPI扩展可以达到零侵入，只需要实现相应的接口，并实现即可被LTS使用，目前开放出来的扩展接口有

1. 对任务队列的扩展，用户可以不选择使用mysql或者mongo作为队列存储，也可以自己实现。
2. 对业务日志记录器的扩展，目前主要支持console，mysql，mongo，用户也可以通过扩展选择往其他地方输送日志。

###4、故障转移
当正在执行任务的TaskTracker宕机之后，JobTracker会立马分配在宕机的TaskTracker的所有任务再分配给其他正常的TaskTracker节点执行。
###5、节点监控
可以对JobTracker，TaskTracker节点进行资源监控，任务监控等，可以实时的在LTS-Admin管理后台查看，进而进行合理的资源调配。
###6、多样化任务执行结果支持
LTS框架提供四种执行结果支持，`EXECUTE_SUCCESS`，`EXECUTE_FAILED`，`EXECUTE_LATER`，`EXECUTE_EXCEPTION`，并对每种结果采取相应的处理机制，譬如重试。

* EXECUTE_SUCCESS: 执行成功,这种情况，直接反馈客户端（如果任务被设置了要反馈给客户端）。
* EXECUTE_FAILED：执行失败，这种情况，直接反馈给客户端，不进行重试。
* EXECUTE_LATER：稍后执行（需要重试），这种情况，不反馈客户端，重试策略采用1min，2min，3min的策略，默认最大重试次数为10次，用户可以通过参数设置修改这个重试次数。
* EXECUTE_EXCEPTION：执行异常, 这中情况也会重试(重试策略，同上)

###7、FailStore容错
采用FailStore机制来进行节点容错，Fail And Store，不会因为远程通信的不稳定性而影响当前应用的运行。具体FailStore说明，请参考概念说明中的FailStore说明。

##项目编译打包
项目主要采用maven进行构建，目前提供shell脚本的打包。
环境依赖：`Java(jdk1.7)` `Maven`

用户使用一般分为两种：
###1、Maven构建
可以通过maven命令将lts的jar包上传到本地仓库中。在父pom.xml中添加相应的repository，并用deploy命令上传即可。具体引用方式可以参考lts中的例子即可。
###2、直接Jar引用
需要将lts的各个模块打包成单独的jar包，并且将所有lts依赖包引入。具体引用哪些jar包可以参考lts中的例子即可。

##JobTracker和LTS-Admin部署
提供`(cmd)windows`和`(shell)linux`两种版本脚本来进行编译和部署:

1、运行根目录下的`sh build.sh`或`build.cmd`脚本，会在`dist`目录下生成`lts-{version}-bin`文件夹
2、下面是其目录结构，其中bin目录主要是JobTracker和LTS-Admin的启动脚本。`jobtracker` 中是 JobTracker的配置文件和需要使用到的jar包，`lts-admin`是LTS-Admin相关的war包和配置文件。
lts-{version}-bin的文件结构

```java
├── bin
│   ├── jobtracker.cmd
│   ├── jobtracker.sh
│   ├── lts-admin.cmd
│   └── lts-admin.sh
├── jobtracker
│   ├── conf
│   │   └── zoo
│   │       ├── jobtracker.cfg
│   │       └── log4j.properties
│   └── lib
│       └── *.jar
├── lts-admin
│   ├── conf
│   │   ├── log4j.properties
│   │   └── lts-admin.cfg
│   ├── lib
│   │   └── *.jar
│   └── lts-admin.war
└── tasktracker
    ├── bin
    │   └── tasktracker.sh
    ├── conf
    │   ├── log4j.properties
    │   └── tasktracker.cfg
    └── lib
        └── *.jar
```	    
        
3、JobTracker启动。如果你想启动一个节点，直接修改下`conf/zoo`下的配置文件，然后运行 `sh jobtracker.sh zoo start`即可，如果你想启动两个JobTracker节点，那么你需要拷贝一份zoo,譬如命名为`zoo2`,修改下`zoo2`下的配置文件，然后运行`sh jobtracker.sh zoo2 start`即可。logs文件夹下生成`jobtracker-zoo.out`日志。        
4、LTS-Admin启动.修改`lts-admin/conf`下的配置，然后运行`bin`下的`sh lts-admin.sh`或`lts-admin.cmd`脚本即可。logs文件夹下会生成`lts-admin.out`日志，启动成功在日志中会打印出访问地址，用户可以通过这个访问地址访问了。

##JobClient（部署）使用
需要引入lts的jar包有`lts-jobclient-{version}.jar`，`lts-core-{version}.jar` 及其它第三方依赖jar。
###API方式启动
```java
JobClient jobClient = new RetryJobClient();
jobClient.setNodeGroup("test_jobClient");
jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
jobClient.start();

// 提交任务
Job job = new Job();
job.setTaskId("3213213123");
job.setParam("shopId", "11111");
job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
// job.setCronExpression("0 0/1 * * * ?");  // 支持 cronExpression表达式
// job.setTriggerTime(new Date()); // 支持指定时间执行
Response response = jobClient.submitJob(job);
```
    
###Spring XML方式启动
```java
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
            <!-- 参数 -->
            <prop key="job.fail.store">leveldb</prop>
        </props>
    </property>
</bean>
```    
###Spring 全注解方式
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
##TaskTracker(部署使用)
需要引入lts的jar包有`lts-tasktracker-{version}.jar`，`lts-core-{version}.jar` 及其它第三方依赖jar。
###定义自己的任务执行类
```java
public class MyJobRunner implements JobRunner {
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        try {
            BizLogger bizLogger = jobContext.getBizLogger();
            // TODO 业务逻辑
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }
}
```
###API方式启动
```java 
TaskTracker taskTracker = new TaskTracker();
taskTracker.setJobRunnerClass(MyJobRunner.class);
taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
taskTracker.setNodeGroup("test_trade_TaskTracker");
taskTracker.setWorkThreads(20);
taskTracker.start();
```
###Spring XML方式启动
```java
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
###Spring注解方式启动
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
//        factoryBean.start();
        return factoryBean.getObject();
    }
}
```
##参数说明

| 参数  | 是否必须  | 默认值 | 使用范围 | 设置方式|参数说明 |
|:------------- |:------------- |:---------------:|:---------------:| -------------:| -------------:|
|registryAddress|必须|无|JobClient,JobTracker,TaskTracker|setRegistryAddress("xxxx")|注册中心，可以选用zk或者redis，参考值: zookeeper://127.0.0.1:2181|
|clusterName|必须|无|JobClient,JobTracker,TaskTracker|setClusterName("xxxx")|集群名称，clusterName相同的所有节点才会组成整个LTS架构|
|listenPort|必须|35001|JobTracker|setListenPort(xxx)|JobTracker的远程监听端口|
|job.logger|必须|console|JobTracker|addConfig("job.logger","xxx")|LTS业务日志记录器，可选值console,mysql,mongo,或者自己实现SPI扩展|
|job.queue|必须|mongo|JobTracker|addConfig("job.queue", "xx")|LTS任务队列,可选值mongo,mysql,或者自己实现SPI扩展|
|jdbc.url|可选|无|JobTracker|addConfig("jdbc.url", "xxx")|mysql连接URL，当job.queue为mysql的时候起作用|
|jdbc.username|可选|无|JobTracker|addConfig("jdbc.username", "xxx")|mysql连接密码,当job.queue为mysql的时候起作用|
|jdbc.password|可选|无|JobTracker|addConfig("jdbc.password", "xxx")|mysql连接密码,当job.queue为mysql的时候起作用|
|mongo.addresses|可选|无|JobTracker|addConfig("mongo.addresses", "xxx")|mongo连接URL,当job.queue为mongo的时候起作用|
|mongo.database|可选|无|JobTracker|addConfig("mongo.database", "xxx")|mongo数据库名,当job.queue为mongo的时候起作用|
|zk.client|可选|zkclient|JobClient,JobTracker,TaskTracker|addConfig("zk.client", "xxx")|zookeeper客户端,可选值zkclient, curator|
|job.pull.frequency|可选|3|TaskTracker|addConfig("job.pull.frequency", "xx")|TaskTracker去向JobTracker Pull任务的频率，针对不同的场景可以做相应的调整，单位秒|
|job.max.retry.times|可选|10|JobTracker|addConfig("job.max.retry.times", "xx")|任务的最大重试次数|
|stop.working|可选|false|TaskTracker|addConfig("stop.working", "true")|主要用于当TaskTracker与JobTracker出现网络隔离的时候，超过一定时间隔离之后，TaskTracker自动停止当前正在运行的任务|



##使用建议
一般在一个JVM中只需要一个JobClient实例即可，不要为每种任务都新建一个JobClient实例，这样会大大的浪费资源，因为一个JobClient可以提交多种任务。相同的一个JVM一般也尽量保持只有一个TaskTracker实例即可，多了就可能造成资源浪费。当遇到一个TaskTracker要运行多种任务的时候，请参考下面的 "一个TaskTracker执行多种任务"。
##一个TaskTracker执行多种任务
有的时候，业务场景需要执行多种任务，有些人会问，是不是要每种任务类型都要一个TaskTracker去执行。我的答案是否定的，如果在一个JVM中，最好使用一个TaskTracker去运行多种任务，因为一个JVM中使用多个TaskTracker实例比较浪费资源（当然当你某种任务量比较多的时候，可以将这个任务单独使用一个TaskTracker节点来执行）。那么怎么才能实现一个TaskTracker执行多种任务呢。下面是我给出来的参考例子。

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
    public Result run(Job job) throws Throwable {
        String type = job.getParam("type");
        return JOB_RUNNER_MAP.get(type).run(job);
    }
}

class JobRunnerA implements JobRunner {
    @Override
    public Result run(Job job) throws Throwable {
        //  TODO A类型Job的逻辑
        return null;
    }
}

class JobRunnerB implements JobRunner {
    @Override
    public Result run(Job job) throws Throwable {
        // TODO B类型Job的逻辑
        return null;
    }
}
```
##SPI扩展说明
###LTS-Logger扩展
1. 引入`lts-core-{version}.jar`
2. 实现`JobLogger`和`JobLoggerFactory`接口
3. 在 resources `META-INF/lts/com.github.ltsopensource.biz.logger.JobLoggerFactory`文件,文件内容为`xxx=com.github.ltsopensource.biz.logger.xxx.XxxJobLoggerFactory`
4. 使用自己的logger扩展，修改jobtracker参数配置 configs.job.logger=xxx。（如果你自己引入JobTracker jar包的方式的话，使用 `jobtracker.addConfig("job.logger", "xxx"))`

###LTS-Queue扩展
实现方式和LTS-Logger扩展类似，具体参考`lts-queue-mysql`或`lts-queue-mongo`模块的实现
##和其它解决方案比较
###和MQ比较
见docs/LTS业务场景说明.pdf
###和Quartz比较
见docs/LTS业务场景说明.pdf



