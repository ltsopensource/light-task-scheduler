package com.lts.monitor.access.mysql;

import com.lts.core.cluster.Config;
import com.lts.store.jdbc.JdbcAbstractAccess;

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
