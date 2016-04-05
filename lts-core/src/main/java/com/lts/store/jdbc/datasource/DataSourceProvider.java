package com.lts.store.jdbc.datasource;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiExtensionKey;

import javax.sql.DataSource;

/**
 * @author Robert HG (254963746@qq.com) on 10/24/14.
 */
@SPI(key = SpiExtensionKey.JDBC_DATASOURCE_PROVIDER, dftValue = "mysql")
public interface DataSourceProvider {

    DataSource getDataSource(Config config);

}
