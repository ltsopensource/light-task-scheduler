package com.lts.job.store.jdbc;

import com.lts.job.core.cluster.Config;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class DataSourceProviderFactory {

    private static final ConcurrentHashMap<String, DataSourceProvider> PROVIDER_MAP = new ConcurrentHashMap<String, DataSourceProvider>();

    static {
        PROVIDER_MAP.put("mysql", new MysqlDataSourceProvider());
        PROVIDER_MAP.put("hsqldb", new HsqlDataSourceProvider());
    }

    public static DataSourceProvider create(Config config) {
        String provider = config.getParameter("jdbc.datasource.provider", "mysql");
        DataSourceProvider dataSourceProvider = PROVIDER_MAP.get(provider);
        if (dataSourceProvider == null) {
            throw new IllegalArgumentException("Can not find jdbc.datasource.provider:" + provider);
        }
        return dataSourceProvider;
    }

}
