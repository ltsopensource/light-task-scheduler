package com.github.ltsopensource.store.jdbc.datasource;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

import javax.sql.DataSource;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
@SPI(key = ExtConfig.JDBC_DATASOURCE_PROVIDER, dftValue = "mysql")
public interface DataSourceProvider {

    DataSource getDataSource(Config config);

}
