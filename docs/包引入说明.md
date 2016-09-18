
###包引入说明

####1. JobTracker,JobClient,TaskTracker都需要引入的包

#####1.1 lts-core

```java
<dependency>
    <groupId>com.github.ltsopensource</groupId>
    <artifactId>lts-core</artifactId>
    <version>${lts版本号}</version>
</dependency>
```

#####1.2 zk客户端包 
二选一, 通过 addConfig("zk.client", "可选值: curator, zkclient, lts") 设置, 如果用lts,可以不用引入包

`zkclient`

```java
<dependency>
    <groupId>com.github.sgroschupf</groupId>
    <artifactId>zkclient</artifactId>
    <version>0.1</version>
</dependency>
```

`curator`

```java
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>2.9.1</version>
</dependency>
```

`zookeeper包`

```java
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>${zk.version}</version>
    <exclusions>
        <exclusion>
            <groupId>org.jboss.netty</groupId>
            <artifactId>netty</artifactId>
        </exclusion>
        <exclusion>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#####1.3 通讯包
netty或者mina, 二选一, 通过 addConfig("lts.remoting", "可选值: netty, mina") 设置

`netty`

```java
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.0.20.Final</version>
</dependency>
```

`mina`

```java
<dependency>
    <groupId>org.apache.mina</groupId>
    <artifactId>mina-core</artifactId>
    <version>2.0.9</version>
</dependency>
```

#####1.4 json包
fastjson或者jackson, 二选一, 通过 addConfig("lts.json", "可选值: fastjson, jackson") 设置

`fastjson`

```java
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.7</version>
</dependency>
```

`jackson`

```java
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.6.3</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.6.3</version>
</dependency>
```

#####1.5 日志包
可以选用 slf4j, jcl, log4j, 或者使用jdk原生logger

LoggerFactory.setLoggerAdapter("可选值: slf4j, jcl, log4j, jdk"), 不手动设置, 默认按这个顺序加载


`log4j`

```java
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.16</version>
</dependency>
```

`slf4j`

```java
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.5</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.5</version>
</dependency>
```

`jcl`

```java
<dependency>
    <groupId>commons-logging</groupId>
    <artifactId>commons-logging-api</artifactId>
    <version>1.1</version>
</dependency>
```

#####1.6 如果需要spring的话,需要引入lts-spring及spring的相关包

```java
<dependency>
    <groupId>com.github.ltsopensource</groupId>
    <artifactId>lts-spring</artifactId>
    <version>${lts版本号}</version>
</dependency>
```

####2. 对于JobTracker端

#####2.1 必须引入的包:

```java
<dependency>
    <groupId>com.github.ltsopensource</groupId>
    <artifactId>lts-jobtracker</artifactId>
    <version>${lts版本号}</version>
</dependency>
```

#####2.2 除了基础包之外还需要引入任务队列的包(可以是mongo或者mysql) 

`mysql`

```java
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.26</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.0.14</version>
</dependency>
```

`mongo`

```java
<dependency>
    <groupId>org.mongodb.morphia</groupId>
    <artifactId>morphia</artifactId>
    <version>1.0.0-rc1</version>
</dependency>
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongo-java-driver</artifactId>
    <version>3.0.2</version>
</dependency>
```

####3. JobClient需要引入的包

必须引入的包

```java
<dependency>
    <groupId>com.github.ltsopensource</groupId>
    <artifactId>lts-jobclient</artifactId>
    <version>${project.version}</version>
</dependency>
```

FailStore存储包(四选一)

通过 jobClient.addConfig("job.fail.store", "可选值: leveldb, mapdb, berkeleydb, rocksdb") 设置

```java
<!--mapdb -->
<dependency>
    <groupId>org.mapdb</groupId>
    <artifactId>mapdb</artifactId>
    <version>2.0-beta10</version>
</dependency>
<!-- leveldb -->
<dependency>
    <groupId>org.fusesource.leveldbjni</groupId>
    <artifactId>leveldbjni-all</artifactId>
    <version>1.2.7<version>
</dependency>
<!-- berkeleydb  -->
<dependency>
    <groupId>com.sleepycat</groupId>
    <artifactId>je</artifactId>
    <version>5.0.73</version>
</dependency>
<!-- rocksdb -->
<dependency>
    <groupId>org.rocksdb</groupId>
    <artifactId>rocksdbjni</artifactId>
    <version>3.10.1</version>
</dependency>
```

####3. TaskTracker需要引入的包

必须引入的包

```java
<dependency>
    <groupId>com.github.ltsopensource</groupId>
    <artifactId>lts-tasktracker</artifactId>
    <version>${project.version}</version>
</dependency>
```

FailStore存储包(四选一)

通过 taskTracker.addConfig("job.fail.store", "可选值: leveldb, mapdb, berkeleydb, rocksdb") 设置

```java
<!--mapdb -->
<dependency>
    <groupId>org.mapdb</groupId>
    <artifactId>mapdb</artifactId>
    <version>2.0-beta10</version>
</dependency>
<!-- leveldb -->
<dependency>
    <groupId>org.fusesource.leveldbjni</groupId>
    <artifactId>leveldbjni-all</artifactId>
    <version>1.2.7<version>
</dependency>
<!-- berkeleydb  -->
<dependency>
    <groupId>com.sleepycat</groupId>
    <artifactId>je</artifactId>
    <version>5.0.73</version>
</dependency>
<!-- rocksdb -->
<dependency>
    <groupId>org.rocksdb</groupId>
    <artifactId>rocksdbjni</artifactId>
    <version>3.10.1</version>
</dependency>
```

