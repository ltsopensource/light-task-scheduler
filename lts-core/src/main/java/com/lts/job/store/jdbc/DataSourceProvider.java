package com.lts.job.store.jdbc;

import com.lts.job.core.cluster.Config;

import javax.sql.DataSource;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
public interface DataSourceProvider {

    public DataSource getDataSource(Config config);

}
