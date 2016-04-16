package com.github.ltsopensource.store.jdbc.builder;

import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class WhereSql {

    private StringBuilder sql = new StringBuilder();
    private List<Object> params = new LinkedList<Object>();
    private boolean isFirstCondition = true;
    private static final String PREFIX = " WHERE ";

    public WhereSql() {
        sql.append(PREFIX);
    }

    public WhereSql and(String condition, Object value) {
        if (!isFirstCondition) {
            sql.append(" AND ");
        }
        isFirstCondition = false;
        sql.append(condition);
        params.add(value);
        return this;
    }

    public WhereSql or(String condition, Object value) {
        if (!isFirstCondition) {
            sql.append(" OR ");
        }
        isFirstCondition = false;
        sql.append(condition);
        params.add(value);
        return this;
    }

    public WhereSql and(String condition) {
        if (!isFirstCondition) {
            sql.append(" AND ");
        }
        isFirstCondition = false;
        sql.append(condition);
        return this;
    }

    public WhereSql or(String condition) {
        if (!isFirstCondition) {
            sql.append(" OR ");
        }
        isFirstCondition = false;
        sql.append(condition);
        return this;
    }

    public WhereSql andOnNotEmpty(String condition, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return and(condition, value);
    }

    public WhereSql orOnNotEmpty(String condition, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return or(condition, value);
    }

    public WhereSql andOnNotNull(String condition, Object value) {
        if (value == null) {
            return this;
        }
        return and(condition, value);
    }

    public WhereSql orOnNotNull(String condition, Object value) {
        if (value == null) {
            return this;
        }
        return or(condition, value);
    }

    public WhereSql andBetween(String column, Object start, Object end) {

        if (start == null && end == null) {
            return this;
        }

        if (!isFirstCondition) {
            sql.append(" AND ");
        }
        isFirstCondition = false;

        if (start != null && end != null) {
            sql.append(" (").append(column).append(" BETWEEN ? AND ? ").append(")");
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

    public WhereSql orBetween(String column, Object start, Object end) {

        if (start == null && end == null) {
            return this;
        }

        if (!isFirstCondition) {
            sql.append(" OR ");
        }
        isFirstCondition = false;

        if (start != null && end != null) {
            sql.append(" (").append(column).append(" BETWEEN ? AND ? ").append(")");
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

    public List<Object> params() {
        return params;
    }

    public String getSQL() {
        String finalSQL = sql.toString();
        if (finalSQL.length() == PREFIX.length()) {
            // 表示没有where条件
            return "";
        }
        return sql.toString();
    }
}
