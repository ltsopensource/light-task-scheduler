package com.lts.job.store.jdbc;

import com.lts.job.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public abstract class JdbcRepository {

    private SqlTemplate sqlTemplate;
    private volatile boolean init = false;

    public JdbcRepository(Config config) {
        if (!init) {
            sqlTemplate = new SqlTemplate(
                    DataSourceProvider.getDataSource(config));
            init = true;
        }
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

}
