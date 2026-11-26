package com.lakbaylokal.database;

import com.lakbaylokal.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections to MySQL database with automatic connection
 * testing, prepared statement support, and graceful error handling.
 */
public class ConnectionFactory {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("[ConnectionFactory] MySQL JDBC Driver not found in classpath. " +
                    "Ensure mysql-connector-j is included. Error: " + e.getMessage());
        }
    }

    /**
     * Builds standard JDBC connection URL.
     */
    public static String getJdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila&characterEncoding=utf8",
                AppConfig.getDbHost(),
                AppConfig.getDbPort(),
                AppConfig.getDbName()
        );
    }

    /**
     * Obtains a new database connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                getJdbcUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPassword()
        );
    }

    /**
     * Tests if the MySQL database server is reachable and credentials are valid.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[ConnectionFactory] Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safely closes a Connection.
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }
}
