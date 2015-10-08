package com.lts.store.mongo;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
public class DataStoreProvider {

    // 同一配置, 始终保持同一个连接
    private static final ConcurrentHashMap<String, Datastore> DATA_STORE_MAP = new ConcurrentHashMap<String, Datastore>();
    // 锁, 防止重复连接同一配置mongo
    private static final Object lock = new Object();

    public static Datastore getDataStore(Config config) {

        String[] addresses = config.getParameter(ADDRESSES_KEY, new String[]{DEFAULT_ADDRESSES});
        String database = config.getParameter(DATABASE_KEY, DEFAULT_DATABASE);
        String username = config.getParameter(USERNAME);
        String pwd = config.getParameter(PASSWORD);

        String cachedKey = StringUtils.concat(StringUtils.concat(addresses), database, username, pwd);

        Datastore datastore = DATA_STORE_MAP.get(cachedKey);
        if (datastore == null) {
            try {
                synchronized (lock) {
                    datastore = DATA_STORE_MAP.get(cachedKey);
                    if (datastore != null) {
                        return datastore;
                    }
                    Morphia morphia = new Morphia();
                    MongoFactoryBean mongoFactoryBean = new MongoFactoryBean(addresses, username, database, pwd);
                    MongoClient mongo = mongoFactoryBean.createInstance();
                    datastore = morphia.createDatastore(mongo, database);
                    DATA_STORE_MAP.put(cachedKey, datastore);
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        StringUtils.format("connect mongo failed! addresses: {}, database: {}",
                                addresses, database), e);
            }
        }
        return datastore;
    }

    private static final String ADDRESSES_KEY = "mongo.addresses";
    private static final String DEFAULT_ADDRESSES = "127.0.0.1:27017";
    private static final String DATABASE_KEY = "mongo.database";
    private static final String DEFAULT_DATABASE = "lts";
    private static final String USERNAME = "mongo.username";
    private static final String PASSWORD = "mongo.password";


}
