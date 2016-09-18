package com.github.ltsopensource.monitor.access.mysql;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.store.jdbc.JdbcAbstractAccess;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public abstract class MysqlAbstractJdbcAccess extends JdbcAbstractAccess {

    public MysqlAbstractJdbcAccess(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/" + getTableName() + ".sql"));
    }

    protected abstract String getTableName();

}
