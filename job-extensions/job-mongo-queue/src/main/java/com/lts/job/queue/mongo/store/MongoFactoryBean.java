package com.lts.job.queue.mongo.store;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 * Mongo 工厂类
 */
public class MongoFactoryBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoFactoryBean.class);
    private List<ServerAddress> replicaSetSeeds = new ArrayList<ServerAddress>();
    private MongoClientOptions mongoClientOptions;

    public MongoFactoryBean(String[] serverAddresses) {
        replSeeds(serverAddresses);
    }

    public MongoFactoryBean(String serverAddresse) {
        replSeeds(new String[]{serverAddresse});
    }

    public MongoFactoryBean(MongoClientOptions mongoClientOptions) {
        this.mongoClientOptions = mongoClientOptions;
    }

    public Mongo createInstance() throws Exception {
        if (replicaSetSeeds.size() > 0) {
            if (mongoClientOptions != null) {
                return new MongoClient(replicaSetSeeds, mongoClientOptions);
            }
            return new MongoClient(replicaSetSeeds);
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
