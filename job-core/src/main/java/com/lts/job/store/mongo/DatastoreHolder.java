package com.lts.job.store.mongo;

import com.google.code.morphia.Datastore;
import com.lts.job.store.Config;

/**
 * Created by Robert HG (254963746@qq.com) on 10/24/14.
 */
public class DatastoreHolder {

    private static final Object lock = new Object();

    private static Config config;
    private static Datastore datastore;

    public static Datastore getDatastore() {
        if (datastore == null) {
            synchronized (lock) {
                if (datastore == null) {
                    datastore = MongoConnectionManager.getDatastore(config);
                }
            }
        }
        return datastore;
    }


    public static void setConfig(Config config) {
        DatastoreHolder.config = config;
    }
}
