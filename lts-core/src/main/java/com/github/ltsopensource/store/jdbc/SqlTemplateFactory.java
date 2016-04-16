package com.github.ltsopensource.store.jdbc;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.store.jdbc.datasource.DataSourceProvider;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 保证一个DataSource对应一个SqlTemplate
 *
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public class SqlTemplateFactory {

    private static final ConcurrentMap<DataSource, SqlTemplate> HOLDER = new ConcurrentHashMap<DataSource, SqlTemplate>();

    public static SqlTemplate create(Config config) {
        DataSourceProvider dataSourceProvider = ServiceLoader.load(DataSourceProvider.class, config);
        DataSource dataSource = dataSourceProvider.getDataSource(config);
        SqlTemplate sqlTemplate = HOLDER.get(dataSource);

        if (sqlTemplate != null) {
            return sqlTemplate;
        }
        synchronized (HOLDER) {
            sqlTemplate = HOLDER.get(dataSource);
            if (sqlTemplate != null) {
                return sqlTemplate;
            }
            sqlTemplate = new SqlTemplateImpl(dataSource);
            HOLDER.putIfAbsent(dataSource, sqlTemplate);
            return sqlTemplate;
        }
    }

}
