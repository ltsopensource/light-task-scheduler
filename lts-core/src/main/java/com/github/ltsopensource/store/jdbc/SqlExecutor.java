package com.github.ltsopensource.store.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 5/20/15.
 */
public interface SqlExecutor<T> {
    T run(Connection conn) throws SQLException;
}