# LTS用户文档
	
LTS(light-task-scheduler)主要用于解决分布式任务调度问题，支持实时任务，定时任务和Cron任务。有较好的伸缩性，扩展性，健壮稳定性而被多家公司使用，同时也希望开源爱好者一起贡献。

## 项目地址
github地址:
[https://github.com/qq254963746/light-task-scheduler](https://github.com/qq254963746/light-task-scheduler)

oschina地址:
[git.oschina.net/hugui/light-task-scheduler](git.oschina.net/hugui/light-task-scheduler)

这两个地址都会同步更新。

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

##JobTracker和LTS-Admin部署
运行根目录下的`sh build.sh`脚本，会在`dist`目录下生成`lts-{version}-bin`文件夹，


