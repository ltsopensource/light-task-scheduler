package com.lts.job.store.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 5/20/15.
 */
public class SqlTemplate {

    private final DataSource dataSource;
    private final QueryRunner QUERY_RUNNER = new QueryRunner();

    public SqlTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(boolean isReadOnly, SqlExecutor<T> executor) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (isReadOnly) {
                conn.setReadOnly(true);
            }
            return executor.run(conn);
        } catch (SQLException e) {
            throw e;
        } finally {
            close(conn);
        }
    }

    private void close(Connection conn) throws SQLException {
        if (conn != null) {
            if (conn.isReadOnly()) {
                conn.setReadOnly(false);  // restore NOT readOnly before return to pool
            }
            conn.close();
        }
    }

    public int[] batchUpdate(final Connection conn, final String sql, final Object[][] params) throws SQLException {
        return QUERY_RUNNER.batch(conn, sql, params);
    }

    public int[] batchUpdate(final String sql, final Object[][] params) throws SQLException {
        return execute(false, new SqlExecutor<int[]>() {
            @Override
            public int[] run(Connection conn) throws SQLException {
                return batchUpdate(conn, sql, params);
            }
        });
    }

    public int update(final String sql, final Object... params) throws SQLException {
        return execute(false, new SqlExecutor<Integer>() {
            @Override
            public Integer run(Connection conn) throws SQLException {
                return update(conn, sql, params);
            }
        });
    }

    public int update(final Connection conn, final String sql, final Object... params) throws SQLException {
        return QUERY_RUNNER.update(conn, sql, params);
    }

    public <T> T query(final String sql, final ResultSetHandler<T> rsh, final Object... params) throws SQLException {
        return execute(true, new SqlExecutor<T>() {
            @Override
            public T run(Connection conn) throws SQLException {
                return query(conn, sql, rsh, params);
            }
        });
    }

    public <T> T query(final Connection conn, final String sql, final ResultSetHandler<T> rsh, final Object... params) throws SQLException {
        return QUERY_RUNNER.query(conn, sql, rsh, params);
    }

    public <T> T queryForValue(final String sql, final Object... params) throws SQLException {
        return query(sql, new ScalarHandler<T>(), params);
    }

    public <T> T queryForValue(final Connection conn, final String sql, final Object... params) throws SQLException {
        return query(conn, sql, new ScalarHandler<T>(), params);
    }

    private SqlExecutor<Void> getWrapperExecutor(final SqlExecutorVoid voidExecutor) {
        return new SqlExecutor<Void>() {
            @Override
            public Void run(Connection conn) throws SQLException {
                voidExecutor.run(conn);
                return null;
            }
        };
    }

    public void executeInTransaction(SqlExecutorVoid executor) {
        executeInTransaction(getWrapperExecutor(executor));
    }

    public <T> T executeInTransaction(SqlExecutor<T> executor) {
        Connection conn = null;
        try {
            conn = TxConnectionFactory.getTxConnection(dataSource);
            T res = executor.run(conn);
            conn.commit();
            return res;
        } catch (Error e) {
            throw rollback(conn, e);
        } catch (Exception e) {
            throw rollback(conn, e);
        } finally {
            TxConnectionFactory.closeTx(conn);
        }
    }

    private StateException rollback(Connection conn, Throwable e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
            return new StateException(e);
        } catch (SQLException se) {
            return new StateException("Unable to rollback transaction", e);
        }
    }
}
