package com.lts.job.store.mongo;

import com.google.code.morphia.Datastore;
import com.lts.job.store.Config;
import com.lts.job.store.ConfigLoader;

/**
 * Created by hugui on 10/24/14.
 */
public class DatastoreHolder {

    private static final Object lock = new Object();

    private static Datastore datastore;

    public static Datastore getDatastore() {
        // 如果为空,表示用户没有手动设置，那么从配置文件中读取
        if (datastore == null) {
            synchronized (lock) {
                if (datastore == null) {
                    Config config = ConfigLoader.getConfig();
                    datastore = MongoConnectionManager.getDatastore(config);
                }
            }
        }
        return datastore;
    }


    public static void setConfig(Config config) {
        datastore = MongoConnectionManager.getDatastore(config);
    }
}
