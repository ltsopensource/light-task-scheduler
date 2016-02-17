package com.lts.web.support.spring;

import com.lts.core.cluster.Config;
import com.lts.core.constant.Constants;
import com.lts.store.jdbc.datasource.DataSourceProvider;
import com.lts.store.jdbc.datasource.DataSourceProviderFactory;
import com.lts.web.cluster.AdminAppContext;
import com.lts.web.support.AppConfigurer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public class DataSourceFactoryBean implements FactoryBean<DataSource>, InitializingBean, DisposableBean {

    private DataSource dataSource;
    @Autowired
    private AdminAppContext appContext;

    @Override
    public DataSource getObject() throws Exception {
        return dataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String dataSourceProvider = AppConfigurer.getProperty("jdbc.datasource.provider", DataSourceProvider.H2);

        if (DataSourceProvider.H2.equals(dataSourceProvider)) {
            Config config = new Config();
            // h2 本地文件
            String monitorDBPath = AppConfigurer.getProperty("lts.admin.data.path", Constants.USER_HOME) + "/.lts/h2/lts-admin";
            config.setParameter("jdbc.datasource.provider", dataSourceProvider);
            // http://h2database.com/html/features.html#connection_modes
            // http://h2database.com/html/features.html#auto_mixed_mode
            config.setParameter("jdbc.url", "jdbc:h2:" + monitorDBPath+";AUTO_SERVER=TRUE");
            config.setParameter("jdbc.username", "lts");
            config.setParameter("jdbc.password", "");
            dataSource = DataSourceProviderFactory.create(config).getDataSource(config);
        } else if (DataSourceProvider.MYSQL.equals(dataSourceProvider)) {
            Config config = appContext.getConfig();
            // mysql
            dataSource = DataSourceProviderFactory.create(config).getDataSource(config);

        } else {
            throw new IllegalArgumentException("Error config : jdbc.datasource.provider");
        }
    }

    @Override
    public void destroy() throws Exception {

    }

}
