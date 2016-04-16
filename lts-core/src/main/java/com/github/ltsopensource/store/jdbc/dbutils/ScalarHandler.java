package com.github.ltsopensource.store.jdbc.dbutils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScalarHandler<T> implements ResultSetHandler<T> {

    private final int columnIndex;

    private final String columnName;

    public ScalarHandler() {
        this(1, null);
    }

    public ScalarHandler(int columnIndex) {
        this(columnIndex, null);
    }

    public ScalarHandler(String columnName) {
        this(1, columnName);
    }

    private ScalarHandler(int columnIndex, String columnName) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T handle(ResultSet rs) throws SQLException {

        if (rs.next()) {
            if (this.columnName == null) {
                return (T) rs.getObject(this.columnIndex);
            }
            return (T) rs.getObject(this.columnName);
        }
        return null;
    }
}
