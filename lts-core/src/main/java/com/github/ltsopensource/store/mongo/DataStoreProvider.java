package com.github.ltsopensource.store.mongo;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
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

        String[] addresses = config.getParameter(ExtConfig.MONGO_ADDRESSES, new String[]{"127.0.0.1:27017"});
        String database = config.getParameter(ExtConfig.MONGO_DATABASE, "lts");
        String username = config.getParameter(ExtConfig.MONGO_USERNAME);
        String pwd = config.getParameter(ExtConfig.MONGO_PASSWORD);

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

}
