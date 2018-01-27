package com.plushnode.atlacore.store.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionGuard<T> implements AutoCloseable, SqlRunnable {
    private static final int ER_LOCK_DEADLOCK = 1213;
    private static final int DEADLOCK_RETRY_TIMES = 3;

    private final Connection connection;
    private final SqlRunnable<T> runnable;

    public TransactionGuard(Connection connection, SqlRunnable<T> runnable) throws SQLException {
        connection.setAutoCommit(false);
        this.connection = connection;
        this.runnable = runnable;
    }

    @Override
    public void close() throws SQLException {
        try {
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            this.connection.rollback();
            throw e;
        } finally {
            this.connection.setAutoCommit(true);
        }
    }

    private T run(int retryCount) throws Exception {
        try {
            return this.runnable.run();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                SQLException sqlException = (SQLException) e;
                if (sqlException.getErrorCode() == ER_LOCK_DEADLOCK && retryCount < DEADLOCK_RETRY_TIMES) {
                    ++retryCount;
                    return this.run(retryCount + 1);
                }
            }
            throw e;
        }
    }

    @Override
    public T run() throws Exception {
        return this.run(0);
    }
}
