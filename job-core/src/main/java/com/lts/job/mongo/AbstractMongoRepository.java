package com.lts.job.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.GenericsUtils;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         通用的mongo存储类
 */
public abstract class AbstractMongoRepository<T> {

    protected Datastore ds;
    private Class<T> clazz = GenericsUtils.getSuperClassGenericType(this.getClass());

    public void connect(Config config) {
        ds = DataStoreHolder.getDataStore(config);
        doCreateTable();
    }

    public Query<T> createQuery() {
        return ds.createQuery(clazz);
    }

    /**
     * 创建表
     */
    protected abstract void doCreateTable();
}