package com.plushnode.atlacore.store.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {
    private final ComboPooledDataSource dataSource;

    static {
        // Hide c3p0 log
        Properties p = new Properties(System.getProperties());
        p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // Off or any other level
        System.setProperties(p);
    }

    public DatabaseManager(String url, String databaseName, String username, String password, String driver) throws PropertyVetoException {
        String createUrl = url;

        if (!driver.contains("sqlite")) {
            // Append newly created database to the url so it's used as part of the connection.
            url += databaseName + "?autoReconnect=true";
        }

        this.dataSource = new ComboPooledDataSource();
        this.dataSource.setDriverClass(driver);
        this.dataSource.setJdbcUrl(url);
        this.dataSource.setUser(username);
        this.dataSource.setPassword(password);
        this.dataSource.setMaxPoolSize(8);
        this.dataSource.setMinPoolSize(2);
        this.dataSource.setMaxStatements(128);
        this.dataSource.setMaxStatementsPerConnection(16);
        this.dataSource.setAcquireRetryAttempts(10);
        this.dataSource.setAcquireIncrement(2);
        this.dataSource.setMaxIdleTime(1800);
        this.dataSource.setIdleConnectionTestPeriod(1500);
        this.dataSource.setTestConnectionOnCheckin(true);

        // create the database after the DataSource so the driver is loaded.
        if (!driver.contains("sqlite")) {
            createDatabase(createUrl, databaseName, username, password);
        }
    }

    private void createDatabase(String url, String databaseName, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE " + databaseName);
            } catch (Exception e) {
                // pass
            }
        } catch (Exception e) {
            // pass
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void initDatabase(String initSqlFileName) {
        InputStream stream = this.getClass().getResourceAsStream("/" + initSqlFileName);
        try (SqlStreamExecutor executor = new SqlStreamExecutor(this.getConnection(), stream)) {
            executor.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
