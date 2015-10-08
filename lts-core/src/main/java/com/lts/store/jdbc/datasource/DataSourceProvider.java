package com.lts.store.jdbc.datasource;

import com.lts.core.cluster.Config;

import javax.sql.DataSource;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
public interface DataSourceProvider {

    String H2 = "h2";

    String MYSQL = "mysql";

    public DataSource getDataSource(Config config);

}
