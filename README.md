LTS 轻量级分布式任务调度框架(Light Task Schedule)
-----------------

## 框架概况：
 LTS是一个轻量级分布式任务调度框架，参考hadoop的部分思想。有三种角色, JobClient, JobTracker, TaskTracker。各个节点都是无状态的，可以部署多个，来实现负载均衡，实现更大的负载量, 并且框架具有很好的容错能力。
 采用Zookeeper暴露节点信息，master选举。Mongo存储任务队列和任务执行日志, netty做底层通信。
* JobClient : 主要负责提交任务, 和 接收任务执行反馈结果。
* JobTracker : 负责接收并分配任务，任务调度。
* TaskTracker: 负责执行任务，执行完反馈给JobTracker。

框架支持实时任务，也支持定时任务，同时也支持CronExpression, 有问题，请联系QQ254963746, 或加入QQ群：109500214 一起探讨

##架构图
![Aaron Swartz](https://raw.githubusercontent.com/qq254963746/light-task-schedule/master/data/%E6%9E%B6%E6%9E%84%E5%9B%BE.png)
##节点组:
* 1. 一个节点组等同于一个集群，同一个节点组中的各个节点是对等的，外界无论连接节点组中的任务一个节点都是可以的。
* 2. 每个节点组中都有一个master节点，采用zookeeper进行master选举(master宕机，会自动选举出新的master节点)，框架会提供接口API来监听master节点的变化，用户可以自己使用master节点做自己想做的事情。
* 3. JobClient和TaskTracker都可以存在多个节点组。譬如 JobClient 可以存在多个节点组。 譬如：JobClient 节点组为 ‘lts_WEB’ 中的一个节点提交提交一个 只有节点组为’lts_TRADE’的 TaskTracker 才能执行的任务。
* 4. (每个集群中)JobTacker只有一个节点组。
* 5. 多个JobClient节点组和多个TaskTracker节点组再加上一个JobTacker节点组, 组成一个大的集群。

## 工作流程:
* 1. JobClient 提交一个 任务 给 JobTracker, 这里我提供了两种客户端API, 一种是如果JobTracker 不存在或者提交失败，直接返回提交失败。另一种客户端是重试客户端, 如果提交失败，先存储到本地leveldb(可以使用NFS来达到同个节点组共享leveldb文件的目的,多线程访问，做了文件锁处理)，返回给客户端提交成功的信息，待JobTracker可用的时候，再将任务提交。
* 2. JobTracker 收到JobClient提交来的任务，先生成一个唯一的JobID。然后将任务储存在Mongo集群中。JobTracker 发现有（任务执行的）可用的TaskTracker节点(组) 之后，将优先级最大，最先提交的任务分发给TaskTracker。这里JobTracker会优先分配给比较空闲的TaskTracker节点，达到负载均衡。
* 3. TaskTracker 收到JobTracker分发来的任务之后，执行。执行完毕之后，再反馈任务执行结果给JobTracker（成功or 失败[失败有失败错误信息]），如果发现JobTacker不可用，那么存储本地leveldb，等待TaskTracker可用的时候再反馈。反馈结果的同时，询问JobTacker有没有新的任务要执行。
* 4. JobTacker收到TaskTracker节点的任务结果信息，生成并插入(mongo)任务执行日志。根据任务信息决定要不要反馈给客户端。不需要反馈的直接删除, 需要反馈的（同样JobClient不可用存储文件，等待可用重发）。
* 5. JobClient 收到任务执行结果，进行自己想要的逻辑处理。

## 特性

* 负载均衡:
     * JobClient 和 TaskTracker会随机连接JobTracker节点组中的一个节点，实现JobTracker负载均衡。当连接上后，将一直保持连接这个节点,保持连接通道，直到这个节点不可用，减少每次都重新连接一个节点带来的性能开销。
     * JobTracker 分发任务时，是优先分配给最空闲的一个TaskTracker节点，实现TaskTracker节点的负载均衡。

* 健壮性:
     * 当节点组中的一个节点当机之后，自动转到其他节点工作。当整个节点组当机之后，将会采用存储文件的方式，待节点组可用的时候进行重发。
     * 当执行任务的TaskTracker节点当机之后，JobTracker 会将这个TaskTracker上的未完成的任务(死任务)，重新分配给节点组中其他节点执行。

* 伸缩性：
     * 因为各个节点都是无状态的，可以动态增加机器部署实例, 节点关注者会自动发现。

## 开发计划：
* WEB后台管理
* 框架优化

## 调用示例
* 安装 zookeeper 和 mongo , 执行 data/mongo 目录下的 mongo.md 中的语句

运行 job-example模块中的例子（包含API启动例子和Spring例子）
分别执行 JobTrackerTest TaskTrackerTest JobClientTest

这里给出的是java API(设置配置)方式启动, 也可以使用spring启动默认不启用spring，需引入job-ext-spring包

## JobTracker 端
```java
    final JobTracker jobTracker = new JobTracker();
    // 节点信息配置
    jobTracker.setZookeeperAddress("localhost:2181");
    // jobTracker.setListenPort(35001); // 默认 35001
    // jobTracker.setClusterName("lts");

    // mongo 配置
    Config config = new Config();
    config.setAddresses(new String[]{"localhost:27017"});
    config.setUsername("lts");
    config.setPassword("lts");
    config.setDbName("job");
    jobTracker.setStoreConfig(config);

    // 启动节点
    jobTracker.start();

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
            jobTracker.stop();
        }
    }));

```
或者Spring配置
```xml
    <bean id="mongoConfig" class="com.lts.job.store.Config">
        <property name="addresses">
            <array>
                <value>localhost:27017</value>
            </array>
        </property>
        <property name="username" value="lts"/>
        <property name="password" value="lts"/>
        <property name="dbName" value="job"/>
    </bean>
    <bean id="jobTracker" class="com.lts.job.spring.JobTrackerFactoryBean" init-method="start">
        <!--<property name="clusterName" value="lts"/>--> <!-- 集群名称 -->
        <!--<property name="listenPort" value="35001"/>--> <!-- 默认 35001 -->
        <property name="zookeeperAddress" value="localhost:2181"/>
        <property name="storeConfig" ref="mongoConfig"/>
        <property name="masterNodeChangeListeners">
            <array>
                <bean class="com.lts.job.example.support.MasterNodeChangeListenerImpl"/>
            </array>
        </property>
    </bean>
```

## TaskTracker端
```java
    TaskTracker taskTracker = new TaskTracker();
    taskTracker.setJobRunnerClass(TestJobRunner.class);
    // jobClient.setClusterName("lts");
    taskTracker.setZookeeperAddress("localhost:2181");
    taskTracker.setNodeGroup("test_trade_TaskTracker");
    taskTracker.setWorkThreads(20);
    taskTracker.start();

    // 任务执行类
    public class TestJobRunner implements JobRunner {

        @Override
        public void run(Job job) throws Throwable {

            System.out.println("我要执行"+ job);
            System.out.println(job.getParam("shopId"));

            try {
                Thread.sleep(5*1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
```
或者Spring方式配置
```xml
    <bean id="taskTracker" class="com.lts.job.spring.TaskTrackerFactoryBean" init-method="start">
        <!--<property name="clusterName" value="lts"/>-->
        <property name="nodeGroup" value="test_trade_TaskTracker"/><!-- 所属节点组名称 -->
        <property name="zookeeperAddress" value="localhost:2181"/>
        <property name="jobRunnerClass" value="com.lts.job.example.support.TestJobRunner"/> <!-- 任务执行类 -->
        <property name="workThreads" value="1"/>    <!-- 工作线程个数 -->
        <property name="masterNodeChangeListeners"> <!-- 所属节点组中master节点变化监听器，可以不用配置 -->
            <array>
                <bean class="com.lts.job.example.support.MasterNodeChangeListenerImpl"/>
            </array>
        </property>
    </bean>
```

## JobClient端
```java
    JobClient jobClient = new RetryJobClient();
    // JobClient jobClient = new JobClient();
    jobClient.setNodeGroup("test_JobClient");
    // jobClient.setClusterName("lts");
    jobClient.setZookeeperAddress("localhost:2181");
    jobClient.start();

    // 提交任务
    Job job = new Job();
    job.setParam("shopId", "11111");
    job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
    // job.setCronExpression("0 0/1 * * * ?");  // 支持 cronExpression表达式
    // job.setTriggerTime(new Date().getTime()); // 支持指定时间执行
    Response response = jobClient.submitJob(job);
```
或者spring方式启动
```xml
   <bean id="jobClient" class="com.lts.job.spring.JobClientFactoryBean" init-method="start">
        <property name="clientType" value="retry"/> <!-- 取值: 为空（默认normal）, normal, retry  -->
        <!--<property name="clusterName" value="lts"/>--> <!-- 默认 defaultCluster -->
        <property name="nodeGroup" value="test_JobClient"/> <!-- 节点组名称 -->
        <property name="zookeeperAddress" value="localhost:2181"/>
        <property name="jobFinishedHandler">
            <bean class="com.lts.job.example.support.JobFinishedHandlerImpl"/>  <!-- 任务完成处理器 -->
        </property>
        <property name="masterNodeChangeListeners"><!-- 所属节点组中master节点变化监听器 -->
            <array>
                <bean class="com.lts.job.example.support.MasterNodeChangeListenerImpl"/>
            </array>
        </property>
    </bean>
```
```java
    // 从Spring容器中取得JobClient Bean
    JobClient jobClient = (JobClient) applicationContext.getBean("jobClient");
    // 提交任务
    Job job = new Job();
    job.setParam("shopId", "11111");
    job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
    // job.setCronExpression("0 0/1 * * * ?");  // 支持 cronExpression表达式
    // job.setTriggerTime(new Date().getTime()); // 支持指定时间执行
    Response response = jobClient.submitJob(job);
```


