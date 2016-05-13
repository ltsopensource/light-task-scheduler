package com.github.ltsopensource.admin.access.memory;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.exception.LtsRuntimeException;
import com.github.ltsopensource.store.jdbc.SqlTemplate;
import com.github.ltsopensource.store.jdbc.SqlTemplateFactory;
import com.github.ltsopensource.store.jdbc.exception.JdbcException;

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
        config.setIdentity(StringUtils.generateUUID());
        config.setParameter("jdbc.datasource.provider", "h2");
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
