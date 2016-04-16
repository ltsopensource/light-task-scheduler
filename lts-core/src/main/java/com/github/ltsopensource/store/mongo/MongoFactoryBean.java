package com.github.ltsopensource.store.mongo;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         Mongo 工厂类
 */
public class MongoFactoryBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoFactoryBean.class);
    private List<ServerAddress> replicaSetSeeds = new ArrayList<ServerAddress>();
    private MongoClientOptions mongoClientOptions;
    private List<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>();

    public MongoFactoryBean(String[] serverAddresses) {
        replSeeds(serverAddresses);
    }

    public MongoFactoryBean(String[] serverAddresses, String username, String database, String pwd) {
        this(serverAddresses, MongoCredential.MONGODB_CR_MECHANISM, username, database, pwd);
    }

    public MongoFactoryBean(String[] serverAddresses, String mechanism, String username, String database, String pwd) {
        replSeeds(serverAddresses);
        if (StringUtils.isNotEmpty(username)) {
            if (MongoCredential.GSSAPI_MECHANISM.equals(mechanism)) {
                mongoCredentials.add(MongoCredential.createGSSAPICredential(username));
            } else {
                mongoCredentials.add(MongoCredential.createMongoCRCredential(username, database, pwd.toCharArray()));
            }
        }
    }

    public MongoFactoryBean(String[] serverAddresses, MongoClientOptions mongoClientOptions) {
        this(serverAddresses);
        this.mongoClientOptions = mongoClientOptions;
    }

    public MongoClient createInstance() throws Exception {
        if (replicaSetSeeds.size() > 0) {
            if (mongoClientOptions != null) {
                if (mongoCredentials != null) {
                    return new MongoClient(replicaSetSeeds, mongoCredentials, mongoClientOptions);
                } else {
                    return new MongoClient(replicaSetSeeds, mongoClientOptions);
                }
            }
            if (mongoCredentials != null) {
                return new MongoClient(replicaSetSeeds, mongoCredentials);
            } else {
                return new MongoClient(replicaSetSeeds);
            }
        }
        return new MongoClient();
    }

    private void replSeeds(String... serverAddresses) {
        try {
            replicaSetSeeds.clear();
            for (String addr : serverAddresses) {
                String[] a = addr.split(":");
                String host = a[0];
                if (a.length > 2) {
                    throw new IllegalArgumentException("Invalid Server Address : " + addr);
                } else if (a.length == 2) {
                    replicaSetSeeds.add(new ServerAddress(host, Integer.parseInt(a[1])));
                } else {
                    replicaSetSeeds.add(new ServerAddress(host));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
