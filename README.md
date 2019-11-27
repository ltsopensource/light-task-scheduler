
# LTS用户文档
	
LTS(light-task-scheduler)主要用于解决分布式任务调度问题，支持实时任务，定时任务和Cron任务。有较好的伸缩性，扩展性，健壮稳定性而被多家公司使用，同时也希望开源爱好者一起贡献。

## 原项目地址
github地址:
[https://github.com/ltsopensource/light-task-scheduler](https://github.com/ltsopensource/light-task-scheduler)

oschina地址:
[http://git.oschina.net/hugui/light-task-scheduler](http://git.oschina.net/hugui/light-task-scheduler)

##1.8.0-SNAPSHOT(master)变更主要点
1. 简化了使用:@LTSScheduled类似spring的@Scheduled直接自动提交任务
2. 简化了使用:OneOffClient可用用来提交一次性任务
3. 简化了使用:提供Docker Image构建脚本和kubernetes部署配置

##打包部署
- sh build.sh # 构建全家桶zip
- 修改lts-1.8.0-SNAPSHOT-bin/conf/下的各个配置
- 启动lts-1.8.0-SNAPSHOT-bin/bin/目录下的脚本
- 一般需要lts-admin * 1 + jobtracker * 2
- jobtracker也可以随应用启动

```java
@SpringBootApplication
@EnableJobTracker
public class Application  {
	...
}

```


##Kubernetes部署
- sh build.sh # 构建全家桶zip
- sh build-docker.sh 10.168.1.136:5000 # 构建docker image, 上传私有仓库
- kubectl apply -f k8s-deploy.yml # 部署kubernetes, 相关参数自行调整


##使用方式
- 这里介绍这个版本特有的方式，其他方式参考原项目

```java
@SpringBootApplication
@EnableLTSScheduled
public class Application  {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    @Bean
    public OneOffJobClient oneOffJobClient(){
    	return new OneOffJobClient();
    }
}
```

```java
@LTS
@Component
public class Jobs {

	@LTSScheduled(value = "unique.business.path", cron="0 0 7,10 * * ?")
	public void doSomethingOnCron() {
		//Do something
	}
	
	@LTSScheduled(value = "unique.business.path2", fixedDelay=60*1000, initialDelay=9000)
	public void doSomethingOnDelay() {
		//Do something
	}
}
```

```java
@Service
public class TradeService {
	@Autowired
	private OneOffJobClient oneOffJobClient;
	
	public void doTrade(){
		//do some business
		
		//check pay result after 3minute
		oneOffJobClient.submitJob("check.pay.job.id", "businessKey"
			, DateTime.now().plusMinutes(3).toDate(), new HashMap<>())
	}
}
```

###maven配置
		<dependency>
			<groupId>com.github.ltsopensource</groupId>
			<artifactId>lts</artifactId>
			<version>1.8.0-SNAPSHOT</version>
		</dependency>
		
###因为很多配置是可选的，默认配置还需要以下依赖(版本号自行调整)
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-all</artifactId>
			<version>4.0.20.Final</version>
		</dependency>
		<dependency>
			<groupId>org.mapdb</groupId>
			<artifactId>mapdb</artifactId>
			<version>2.0-beta10</version>
		</dependency>
		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>druid</artifactId>
			<version>1.0.14</version>
		</dependency>
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-validator</artifactId>
			<version>5.3.4.Final</version>
		</dependency>
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>5.1.41</version>
		</dependency>
		<dependency>
			<groupId>com.101tec</groupId>
			<artifactId>zkclient</artifactId>
			<version>0.4</version> 
			<exclusions>
				<exclusion>
		            <groupId>io.netty</groupId>
		            <artifactId>netty</artifactId>
	        		</exclusion>
	        </exclusions>
		</dependency>
		<dependency>
			<groupId>org.javassist</groupId>
			<artifactId>javassist</artifactId>
			<version>3.21.0-GA</version>
		</dependency>
