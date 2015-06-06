package com.lts.job.web.support.db;

import com.lts.job.core.cluster.Config;
import com.lts.job.store.jdbc.DataSourceProviderFactory;
import com.lts.job.store.jdbc.SqlTemplate;

/**
 * Memory-Only Databases , HSQLDB
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public abstract class MemDB {

    private SqlTemplate sqlTemplate;
    private volatile boolean init = false;

    public MemDB() {
        if (!init) {
            Config config = new Config();
            config.setParameter("jdbc.datasource.provider", "hsqldb");
            config.setParameter("jdbc.url", "jdbc:hsqldb:mem:lts");
            config.setParameter("jdbc.username", "sa");
            config.setParameter("jdbc.password", "");
            sqlTemplate = new SqlTemplate(
                    DataSourceProviderFactory.create(config)
                            .getDataSource(config));
            init = true;
        }
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }
}
