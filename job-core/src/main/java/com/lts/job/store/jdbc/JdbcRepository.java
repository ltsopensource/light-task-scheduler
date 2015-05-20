package com.lts.job.store.jdbc;

import com.lts.job.core.cluster.Config;

import javax.sql.DataSource;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public abstract class JdbcRepository {

    private SqlTemplate sqlTemplate;
    private DataSource dataSource;
    private volatile boolean init = false;

    public JdbcRepository(Config config) {
        if (!init) {
            // 创建DataSource
            dataSource = DataSourceProvider.getDataSource(config);
            sqlTemplate = new SqlTemplate(dataSource);
            init = true;
        }
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
