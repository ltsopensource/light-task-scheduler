package com.github.ltsopensource.store.jdbc.datasource;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
public class H2DataSourceProvider implements DataSourceProvider {

    // 同一配置, 始终保持同一个连接
    private static final ConcurrentHashMap<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<String, DataSource>();

    private static final Object lock = new Object();

    public DataSource getDataSource(Config config) {

        String url = config.getParameter(ExtConfig.JDBC_URL);
        String username = config.getParameter(ExtConfig.JDBC_USERNAME);
        String password = config.getParameter(ExtConfig.JDBC_PASSWORD);

        String cachedKey = StringUtils.concat(url, username, password);

        DataSource dataSource = DATA_SOURCE_MAP.get(cachedKey);
        if (dataSource == null) {
            try {
                synchronized (lock) {
                    dataSource = DATA_SOURCE_MAP.get(cachedKey);
                    if (dataSource != null) {
                        return dataSource;
                    }
                    dataSource = createDataSource(config);

                    DATA_SOURCE_MAP.put(cachedKey, dataSource);
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        StringUtils.format("connect datasource failed! url: {}", url), e);
            }
        }
        return dataSource;
    }

    private DataSource createDataSource(Config config) throws ClassNotFoundException {

        String url = config.getParameter(ExtConfig.JDBC_URL);
        String username = config.getParameter(ExtConfig.JDBC_USERNAME);
        String password = config.getParameter(ExtConfig.JDBC_PASSWORD);

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return dataSource;
    }

}
