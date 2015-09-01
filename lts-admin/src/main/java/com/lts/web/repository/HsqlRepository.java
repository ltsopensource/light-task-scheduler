package com.lts.web.repository;

import com.lts.core.cluster.Config;
import com.lts.core.constant.Constants;
import com.lts.store.jdbc.DataSourceProviderFactory;
import com.lts.store.jdbc.SqlTemplate;
import com.lts.web.support.AppConfigurer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
public class HsqlRepository {

    private SqlTemplate sqlTemplate;
    private AtomicBoolean init = new AtomicBoolean(false);

    public HsqlRepository() {
        String monitorDBPath = AppConfigurer.getProperties("monitor.db.path", Constants.USER_HOME)
                + "/.lts/hsqldb/lts-admin";

        if (init.compareAndSet(false, true)) {
            Config config = new Config();
            config.setParameter("jdbc.datasource.provider", "hsqldb");
            config.setParameter("jdbc.url", "jdbc:hsqldb:file:" + monitorDBPath);
            config.setParameter("jdbc.username", "sa");
            config.setParameter("jdbc.password", "");
            sqlTemplate = new SqlTemplate(
                    DataSourceProviderFactory.create(config)
                            .getDataSource(config));
        }
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

}
