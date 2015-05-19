package com.lts.job.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.StringUtils;
import com.mongodb.Mongo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
public class DataStoreHolder {

    // 同一配置, 始终保持同一个连接
    private static ConcurrentHashMap<String, Datastore> connectionMap = new ConcurrentHashMap<String, Datastore>();
    // 锁, 防止重复连接同一配置mongo
    private static Object lock = new Object();

    public static Datastore getDataStore(Config config) {

        String[] addresses = config.getParameter(ADDRESSES_KEY, new String[]{DEFAULT_ADDRESSES});
        String database = config.getParameter(DATABASE_KEY, DEFAULT_DATABASE);

        String cachedKey = StringUtils.concat(addresses, database);

        Datastore datastore = connectionMap.get(cachedKey);
        if (datastore == null) {
            try {
                synchronized (lock) {
                    datastore = connectionMap.get(cachedKey);
                    if (datastore != null) {
                        return datastore;
                    }
                    Morphia morphia = new Morphia();
                    MongoFactoryBean mongoFactoryBean = new MongoFactoryBean(addresses);
                    Mongo mongo = mongoFactoryBean.createInstance();
                    datastore = morphia.createDatastore(mongo, database);
                    connectionMap.put(cachedKey, datastore);
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        StringUtils.format("connect mongo failed! addresses: {}, database: {}",
                                addresses, database), e);
            }
        }
        return datastore;
    }

    private static String ADDRESSES_KEY = "mongo.addresses";
    private static String DEFAULT_ADDRESSES = "127.0.0.1:27017";
    private static String DATABASE_KEY = "mongo.database";
    private static String DEFAULT_DATABASE = "lts";
}
