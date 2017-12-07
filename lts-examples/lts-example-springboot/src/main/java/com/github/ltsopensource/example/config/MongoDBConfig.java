package com.github.ltsopensource.example.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;


@Configuration
@ComponentScan(basePackages = "com.github.ltsopensource.example.service")
public class MongoDBConfig {
    private Logger logger = LoggerFactory.getLogger(MongoDBConfig.class);
    @Value("${lts.mongodb.servers}")
    private String servers;
    @Value("${lts.mongodb.database}")
	private String database;
    @Value("${lts.mongodb.schema}")
    private String schema;
	@Value("${lts.mongodb.username}")
	private String username;
	@Value("${lts.mongodb.password}")
	private String password;
	@Value("${lts.mongodb.max_wait_time}")
	private int maxWaitTime=24*60;

	@Bean(name="session")
    public MongoDatabase init() throws Exception {
        logger.info("-----mongodb config init.-------");
        List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		if (servers != null && !"".equals(servers)) {
			for (String server : servers.split(",")) {
				String[] address = server.split(":");
				String ip = address[0];
				int port = 27017;
				if (address != null && address.length > 1) {
					port = Integer.valueOf(address[1]);
				}
				seeds.add(new ServerAddress(ip, port));
			}
		}
		Builder builder = new MongoClientOptions.Builder();
		if(maxWaitTime>0){
			builder.maxWaitTime(maxWaitTime*60*1000);
		}
		// 通过连接认证获取MongoDB连接
		@SuppressWarnings("resource")
		MongoClient client = new MongoClient(seeds, builder.build());
		if(!StringUtils.isEmpty(username)){
			MongoCredential credential = MongoCredential.createScramSha1Credential(username, database,password.toCharArray());
			List<MongoCredential> credentials = new ArrayList<MongoCredential>();
			credentials.add(credential);
			client = new MongoClient(seeds, credentials, builder.build());
		}
		MongoDatabase connect = client.getDatabase(schema);// 连接到数据库
        return connect;
    }

}