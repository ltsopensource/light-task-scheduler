package com.lts.web.support.memorydb;

import com.lts.core.cluster.Config;
import com.lts.store.jdbc.DataSourceProviderFactory;
import com.lts.store.jdbc.SqlTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Memory-Only Databases , HSQLDB
 *
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public abstract class MemoryDB {

    private SqlTemplate sqlTemplate;
    private AtomicBoolean init = new AtomicBoolean(false);

    public MemoryDB() {
        if (init.compareAndSet(false, true)) {
            Config config = new Config();
            config.setParameter("jdbc.datasource.provider", "hsqldb");
            config.setParameter("jdbc.url", "jdbc:hsqldb:mem:lts");
            config.setParameter("jdbc.username", "sa");
            config.setParameter("jdbc.password", "");
            sqlTemplate = new SqlTemplate(
                    DataSourceProviderFactory.create(config)
                            .getDataSource(config));
        }
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }
}
