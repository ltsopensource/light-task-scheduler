package com.lts.store.jdbc.datasource;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
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

        String url = config.getParameter(URL_KEY);
        String username = config.getParameter(USERNAME_KEY);
        String password = config.getParameter(PASSWORD_KEY);

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

        String url = config.getParameter(URL_KEY);
        String username = config.getParameter(USERNAME_KEY);
        String password = config.getParameter(PASSWORD_KEY);

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    private static final String URL_KEY = "jdbc.url";
    private static final String USERNAME_KEY = "jdbc.username";
    private static final String PASSWORD_KEY = "jdbc.password";

}
