package com.github.ltsopensource.store.mongo;

import com.github.ltsopensource.core.cluster.Config;
import org.mongodb.morphia.AdvancedDatastore;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         通用的mongo存储类
 */
public abstract class MongoRepository {

    protected final MongoTemplate template;

    public MongoRepository(Config config) {
        this.template = new MongoTemplate(
                (AdvancedDatastore) DataStoreProvider.getDataStore(config));
    }

    public MongoTemplate getTemplate() {
        return template;
    }

    public String getTableName() {
        return template.getDefaultCollName();
    }

    public void setTableName(String tableName) {
        template.setDefaultCollName(tableName);
    }
}