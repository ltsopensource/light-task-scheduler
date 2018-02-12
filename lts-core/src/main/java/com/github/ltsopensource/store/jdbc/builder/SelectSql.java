package com.github.ltsopensource.store.jdbc.builder;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.store.jdbc.SQLFormatter;
import com.github.ltsopensource.store.jdbc.SqlTemplate;
import com.github.ltsopensource.store.jdbc.dbutils.ResultSetHandler;
import com.github.ltsopensource.store.jdbc.exception.JdbcException;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public class SelectSql {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSql.class);

    private SqlTemplate sqlTemplate;
    private StringBuilder sql = new StringBuilder();
    private List<Object> params = new LinkedList<Object>();
    private int curOrderByColumnSize = 0;
    private static final String ORDER_BY = " ORDER BY ";

    public SelectSql(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public SelectSql select() {
        sql.append(" SELECT ");
        return this;
    }

    public SelectSql all() {
        sql.append(" * ");
        return this;
    }

    public SelectSql columns(String... columns) {
        if (columns == null || columns.length == 0) {
            throw new JdbcException("columns must have length");
        }

        String split = "";
        for (String column : columns) {
            sql.append(split);
            split = ",";
            sql.append(column.trim()).append(" ");
        }
        return this;
    }

    public SelectSql from() {
        sql.append(" FROM ");
        return this;
    }

    public SelectSql table(String table) {
        sql.append("`").append(table).append("`");
        return this;
    }

    public SelectSql tables(String... tables) {
        String split = "";
        for (String table : tables) {
            sql.append(split);
            split = ",";
            sql.append(table.trim()).append(" ");
        }
        return this;
    }

    public SelectSql where(){
        sql.append(" WHERE ");
        return this;
    }

    public SelectSql whereSql(WhereSql whereSql) {
        sql.append(whereSql.getSQL());
        params.addAll(whereSql.params());
        return this;
    }

    public SelectSql where(String condition, Object value) {
        sql.append(" WHERE ").append(condition);
        params.add(value);
        return this;
    }

    public SelectSql and(String condition, Object value) {
        sql.append(" AND ").append(condition);
        params.add(value);
        return this;
    }

    public SelectSql or(String condition, Object value) {
        sql.append(" OR ").append(condition);
        params.add(value);
        return this;
    }

    public SelectSql orderBy() {
        curOrderByColumnSize = 0;
        return this;
    }

    public SelectSql column(String column, OrderByType order) {

        if (StringUtils.isEmpty(column) || order == null) {
            return this;
        }

        if (curOrderByColumnSize == 0) {
            sql.append(ORDER_BY);
        } else if (curOrderByColumnSize > 0) {
            sql.append(" , ");
        }
        sql.append(" ").append(column).append(" ").append(order);
        curOrderByColumnSize++;
        return this;
    }

    public SelectSql and(String condition) {
        sql.append(" AND ").append(condition);
        return this;
    }

    public SelectSql or(String condition) {
        sql.append(" OR ").append(condition);
        return this;
    }

    public SelectSql andOnNotNull(String condition, Object value) {
        if (value == null) {
            return this;
        }
        return and(condition, value);
    }

    public SelectSql orOnNotNull(String condition, Object value) {
        if (value == null) {
            return this;
        }
        return or(condition, value);
    }

    public SelectSql andOnNotEmpty(String condition, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return and(condition, value);
    }

    public SelectSql orOnNotEmpty(String condition, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return or(condition, value);
    }

    public SelectSql andBetween(String column, Object start, Object end) {

        if (start == null && end == null) {
            return this;
        }

        if (start != null && end != null) {
            sql.append(" AND (").append(column).append(" BETWEEN ? AND ? ").append(")");
            params.add(start);
            params.add(end);
            return this;
        }

        if (start == null) {
            sql.append(" ").append(column).append(" <= ? ");
            params.add(end);
            return this;
        }

        sql.append("").append(column).append(" >= ? ");
        params.add(start);
        return this;
    }

    public SelectSql orBetween(String column, Object start, Object end) {

        if (start == null && end == null) {
            return this;
        }

        if (start != null && end != null) {
            sql.append(" OR (").append(column).append(" BETWEEN ? AND ? ").append(")");
            params.add(start);
            params.add(end);
            return this;
        }

        if (start == null) {
            sql.append(column).append(" <= ? ");
            params.add(end);
            return this;
        }

        sql.append(column).append(" >= ? ");
        params.add(start);
        return this;
    }

    public SelectSql limit(int start, int size) {
        sql.append(" LIMIT ").append(start).append(",").append(size);
        return this;
    }

    public SelectSql groupBy(String... columns) {
        sql.append(" GROUP BY ");
        String split = "";
        for (String column : columns) {
            sql.append(split);
            split = ",";
            sql.append(column.trim()).append(" ");
        }
        return this;
    }

    public SelectSql having(String condition) {
        sql.append(" HAVING ").append(condition);
        return this;
    }

    public SelectSql innerJoin(String condition) {
        sql.append(" INNER JOIN ").append(condition);
        return this;
    }

    public SelectSql rightOuterJoin(String condition) {
        sql.append(" RIGHT OUTER JOIN ").append(condition);
        return this;
    }

    public SelectSql leftOuterJoin(String condition) {
        sql.append(" LEFT OUTER JOIN ").append(condition);
        return this;
    }

    public <T> List<T> list(ResultSetHandler<List<T>> handler) {
        try {
            return sqlTemplate.query(getSQL(), handler, params.toArray());
        } catch (Exception e) {
            throw new JdbcException("Select SQL Error:" + getSQL(), e);
        }
    }

    public <T> T single(ResultSetHandler<T> handler) {
        try {
            return sqlTemplate.query(getSQL(), handler, params.toArray());
        } catch (Exception e) {
            throw new JdbcException("Select SQL Error:" + getSQL(), e);
        }
    }

    public <T> T single() {
        String finalSQL = getSQL();
        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SQLFormatter.format(finalSQL));
            }

            return sqlTemplate.queryForValue(finalSQL, params.toArray());
        } catch (Exception e) {
            throw new JdbcException("Select SQL Error:" + SQLFormatter.format(finalSQL), e);
        }
    }

    public String getSQL() {
        return sql.toString();
    }
}