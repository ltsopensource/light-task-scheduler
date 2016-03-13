package com.lts.admin.access.memory;

import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.constant.Constants;
import com.lts.core.exception.LtsRuntimeException;
import com.lts.store.jdbc.SqlTemplate;
import com.lts.store.jdbc.SqlTemplateFactory;
import com.lts.store.jdbc.datasource.DataSourceProvider;
import com.lts.store.jdbc.exception.JdbcException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Memory-Only Databases , HSQLDB
 *
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public abstract class MemoryAccess {

    private SqlTemplate sqlTemplate;

    public MemoryAccess() {
        Config config = new Config();
        config.setParameter("jdbc.datasource.provider", DataSourceProvider.H2);
        // see http://www.h2database.com/html/features.html#in_memory_databases
        config.setParameter("jdbc.url", "jdbc:h2:mem:lts_admin;DB_CLOSE_DELAY=-1");
        config.setParameter("jdbc.username", "lts");
        config.setParameter("jdbc.password", "lts");
        sqlTemplate = SqlTemplateFactory.create(config);
    }

    protected SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

    protected String readSqlFile(String path) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            return FileUtils.read(is, Constants.CHARSET);
        } catch (IOException e) {
            throw new LtsRuntimeException("Read sql file : [" + path + "] error ", e);
        }
    }

    protected void createTable(String sql) throws JdbcException {
        try {
            getSqlTemplate().createTable(sql);
        } catch (Exception e) {
            throw new JdbcException("Create table error, sql=" + sql, e);
        }
    }
}
