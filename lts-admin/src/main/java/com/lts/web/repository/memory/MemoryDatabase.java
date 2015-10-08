package com.lts.web.repository.memory;

import com.lts.core.cluster.Config;
import com.lts.store.jdbc.datasource.DataSourceProvider;
import com.lts.store.jdbc.datasource.DataSourceProviderFactory;
import com.lts.store.jdbc.SqlTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Memory-Only Databases , HSQLDB
 *
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public abstract class MemoryDatabase {

    private SqlTemplate sqlTemplate;
    private AtomicBoolean init = new AtomicBoolean(false);

    public MemoryDatabase() {
        if (init.compareAndSet(false, true)) {
            Config config = new Config();
            config.setParameter("jdbc.datasource.provider", DataSourceProvider.H2);
            // see http://www.h2database.com/html/features.html#in_memory_databases
            config.setParameter("jdbc.url", "jdbc:h2:mem:lts_admin;DB_CLOSE_DELAY=-1");
            config.setParameter("jdbc.username", "lts");
            config.setParameter("jdbc.password", "lts");
            sqlTemplate = new SqlTemplate(
                    DataSourceProviderFactory.create(config)
                            .getDataSource(config));
        }
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }
}
