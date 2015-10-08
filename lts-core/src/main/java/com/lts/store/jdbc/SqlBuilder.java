package com.lts.store.jdbc;

import com.lts.core.commons.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class SqlBuilder {

    private String sqlPrefix;
    private StringBuilder setSQL;
    private StringBuilder conditionSQL;
    private StringBuilder orderBySQL;
    private StringBuilder limitSQL;
    private List<Object> params;

    public SqlBuilder(String sqlPrefix) {
        this.sqlPrefix = sqlPrefix;
    }

    public SqlBuilder addCondition(String key, Object obj, String operate) {
        if (!checkCondition(obj)) {
            return this;
        }
        if (conditionSQL == null) {
            conditionSQL = new StringBuilder();
        }
        conditionSQL.append("AND ").append(key).append(operate).append("? ");
        if (params == null) {
            params = new ArrayList<Object>();
        }
        params.add(obj);
        return this;
    }

    public SqlBuilder addUpdateField(String key, Object obj) {
        return addUpdateField(key, obj, true);
    }

    public SqlBuilder addUpdateField(String key, Object obj, boolean checkNull) {
        if (checkNull && !checkCondition(obj)) {
            return this;
        }
        if (setSQL == null) {
            setSQL = new StringBuilder();
        }
        setSQL.append(",").append(key).append("=?");
        if (params == null) {
            params = new ArrayList<Object>();
        }
        params.add(obj);
        return this;
    }

    private boolean checkCondition(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof String) {
            if (StringUtils.isEmpty((String) obj)) {
                return false;
            }
        } else if (
                obj instanceof Boolean ||
                        obj instanceof Integer ||
                        obj instanceof Long ||
                        obj instanceof Float ||
                        obj instanceof Date) {
            return true;
        } else {
            throw new IllegalArgumentException("Can not support type " + obj.getClass());
        }

        return true;
    }

    public SqlBuilder addCondition(String key, Object obj) {
        return addCondition(key, obj, "=");
    }

    public String getSQL() {
        StringBuilder fineSQL = new StringBuilder();
        fineSQL.append(sqlPrefix);

        if (setSQL != null) {
            setSQL.delete(0, 1);
            fineSQL.append(" SET ").append(setSQL);
        }

        if (conditionSQL != null) {
            conditionSQL.delete(0, 3);
            fineSQL.append(" WHERE ").append(conditionSQL);
        }
        if (orderBySQL != null) {
            orderBySQL.delete(0, 1);
            fineSQL.append(" ORDER BY ").append(orderBySQL);
        }
        if (limitSQL != null) {
            fineSQL.append(limitSQL);
        }
        return fineSQL.toString();
    }

    public SqlBuilder addOrderBy(String filed, String direction) {
        if (StringUtils.isNotEmpty(filed) && StringUtils.isNotEmpty(direction)) {
            if (orderBySQL == null) {
                orderBySQL = new StringBuilder();
            }
            orderBySQL.append(", ").append(filed).append(" ").append(direction);
        }
        return this;
    }

    public SqlBuilder addLimit(int start, int limit) {
        if (limitSQL == null) {
            limitSQL = new StringBuilder();
        }
        limitSQL.append(" LIMIT ").append(start).append(",").append(limit);
        return this;
    }

    public List<Object> getParams() {
        return params == null ? new ArrayList<Object>(0) : params;
    }
}
