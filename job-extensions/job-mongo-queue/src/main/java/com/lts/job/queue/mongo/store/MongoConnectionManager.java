package com.lts.job.queue.mongo.store;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 * mongo 连接管理器
 */
public class MongoConnectionManager {

    // 同一配置, 始终保持同一个连接
    private static ConcurrentHashMap<Config, Datastore> connectionMap = new ConcurrentHashMap<Config, Datastore>();
    // 锁, 防止重复连接同一配置mongo
    private static Object lock = new Object();

    public static Datastore getDatastore(Config config) throws DataStoreException {
        Datastore datastore = connectionMap.get(config);
        if (datastore == null) {
            try {
                synchronized (lock) {
                    datastore = connectionMap.get(config);
                    if (datastore != null) {
                        return datastore;
                    }
                    Morphia morphia = new Morphia();
                    MongoFactoryBean mongoFactoryBean = new MongoFactoryBean(config.getAddresses());
                    Mongo mongo = mongoFactoryBean.createInstance();
                    datastore = new DatastoreFactoryBean(
                            morphia, mongo, config.getDbName(), config.getUsername(), config.getPassword()).createInstance();
                    connectionMap.put(config, datastore);
                }
            } catch (Exception e) {
                throw new DataStoreException("mongo datastore init failed! ", e);
            }
        }
        return datastore;
    }

}
