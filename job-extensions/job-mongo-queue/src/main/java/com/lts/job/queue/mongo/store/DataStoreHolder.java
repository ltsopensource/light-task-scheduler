package com.lts.job.queue.mongo.store;

import com.google.code.morphia.Datastore;

/**
 * Created by Robert HG (254963746@qq.com) on 10/24/14.
 */
public class DataStoreHolder {

    public static Datastore getDataStore(Config config) {
        return MongoConnectionManager.getDatastore(config);
    }
}
