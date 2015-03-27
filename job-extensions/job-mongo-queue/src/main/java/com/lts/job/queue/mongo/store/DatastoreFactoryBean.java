package com.lts.job.queue.mongo.store;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 * Datastore 工厂类
 */
public class DatastoreFactoryBean {

    private Morphia morphia;
    private Mongo mongo;
    private String dbName;
    private String username;
    private String password;

    public DatastoreFactoryBean(Morphia morphia, Mongo mongo, String dbName, String username, String password) {
        this.morphia = morphia;
        this.mongo = mongo;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
    }

    public Datastore createInstance() {
        if (username != null){
            return morphia.createDatastore(mongo, dbName, username, password.toCharArray());
        }
        return morphia.createDatastore(mongo, dbName);
    }

}
