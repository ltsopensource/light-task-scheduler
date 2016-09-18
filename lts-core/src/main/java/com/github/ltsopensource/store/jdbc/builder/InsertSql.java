package com.github.ltsopensource.store.jdbc.builder;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.store.jdbc.SQLFormatter;
import com.github.ltsopensource.store.jdbc.SqlTemplate;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;
import com.github.ltsopensource.store.jdbc.exception.JdbcException;
import com.github.ltsopensource.store.jdbc.exception.TableNotExistException;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public class InsertSql {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSql.class);

    private SqlTemplate sqlTemplate;
    private StringBuilder sql = new StringBuilder();
    private List<Object[]> params = new LinkedList<Object[]>();
    private int columnsSize = 0;

    public InsertSql(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public InsertSql insert(String table) {
        this.sql.append("INSERT INTO ");
        sql.append("`").append(table).append("`");
        return this;
    }

    public InsertSql insertIgnore(String table) {
        this.sql.append("INSERT IGNORE INTO ");
        sql.append("`").append(table).append("`");
        return this;
    }

    public InsertSql columns(String... columns) {
        if (columns == null || columns.length == 0) {
            throw new JdbcException("columns must have length");
        }
        if (columnsSize > 0) {
            throw new JdbcException("columns already set");
        }

        columnsSize = columns.length;

        sql.append("(");
        String split = "";
        for (String column : columns) {
            sql.append(split);
            split = ", ";
            sql.append("`").append(column.trim()).append("`");
        }
        sql.append(") VALUES ");

        sql.append("(");
        split = "";
        for (int i = 0; i < columnsSize; i++) {
            sql.append(split);
            split = ",";
            sql.append("?");
        }
        sql.append(")");
        return this;
    }

    public InsertSql values(Object... values) {
        if (values == null || values.length != columnsSize) {
            throw new JdbcException("values.length must eq columns.length");
        }
        params.add(values);
        return this;
    }

    public int doInsert() {

        if (params.size() == 0) {
            throw new JdbcException("No values");
        }
        if (params.size() > 1) {
            throw new JdbcException("values.length gt 1, please use doBatchInsert");
        }

        String execSql = sql.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(execSql);
        }
        try {
            return sqlTemplate.insert(execSql, params.get(0));
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DupEntryException("Insert SQL Error:" + execSql, e);
            } else if (e.getMessage().contains("doesn't exist Query:")) {
                throw new TableNotExistException("Insert SQL Error:" + execSql, e);
            }
            throw new JdbcException("Insert SQL Error:" + execSql, e);
        } catch (Exception e) {
            throw new JdbcException("Insert SQL Error:" + execSql, e);
        }
    }

    public int[] doBatchInsert() {

        if (params.size() == 0) {
            throw new JdbcException("No values");
        }

        String finalSQL = sql.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SQLFormatter.format(finalSQL));
        }

        try {
            Object[][] objects = new Object[params.size()][columnsSize];
            for (int i = 0; i < params.size(); i++) {
                objects[i] = params.get(i);
            }
            return sqlTemplate.batchInsert(finalSQL, objects);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DupEntryException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
            } else if (e.getMessage().contains("doesn't exist Query:")) {
                throw new TableNotExistException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
            }
            throw new JdbcException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
        } catch (Exception e) {
            throw new JdbcException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
        }
    }

    public String getSQL() {
        return sql.toString();
    }

}
