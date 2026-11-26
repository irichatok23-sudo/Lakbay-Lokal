package com.lakbaylokal.database;

import com.lakbaylokal.config.AppConfig;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages JDBC connections to MySQL database with automatic connection
 * testing, dynamic JDBC driver fallback loading, MySQL service auto-wake,
 * and graceful error handling.
 */
public class ConnectionFactory {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static volatile boolean mysqlWakeAttempted = false;

    static {
        loadJdbcDriver();
    }

    private static void loadJdbcDriver() {
        try {
            Class.forName(DRIVER_CLASS);
            return;
        } catch (ClassNotFoundException ignored) {
        }

        // Fallback: dynamically load lib/mysql-connector-j.jar if IDE launched without lib/* on classpath
        String[] candidatePaths = {
                "lib/mysql-connector-j.jar",
                "lib/mysql-connector-j-8.3.0.jar",
                "../lib/mysql-connector-j.jar"
        };
        for (String path : candidatePaths) {
            File jarFile = new File(path);
            if (jarFile.exists() && jarFile.isFile()) {
                try {
                    URL jarUrl = jarFile.toURI().toURL();
                    URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, ConnectionFactory.class.getClassLoader());
                    Class<?> driverClass = Class.forName(DRIVER_CLASS, true, loader);
                    Driver driverInstance = (Driver) driverClass.getDeclaredConstructor().newInstance();
                    DriverManager.registerDriver(new DriverShim(driverInstance));
                    System.out.println("[ConnectionFactory] Dynamically loaded MySQL JDBC Driver from " + jarFile.getPath());
                    return;
                } catch (Exception ex) {
                    System.err.println("[ConnectionFactory] Failed loading JDBC jar from " + path + ": " + ex.getMessage());
                }
            }
        }
        System.err.println("[ConnectionFactory] MySQL JDBC Driver not found in classpath or lib/ directory.");
    }

    private static synchronized void ensureMySqlRunning() {
        if (isPortOpen(AppConfig.getDbHost(), AppConfig.getDbPort(), 400)) {
            return;
        }
        if (mysqlWakeAttempted) {
            return;
        }
        mysqlWakeAttempted = true;

        String[] mysqldPaths = {
                "C:\\wamp64\\bin\\mysql\\mysql8.4.7\\bin\\mysqld.exe",
                "C:\\xampp\\mysql\\bin\\mysqld.exe"
        };
        for (String exePath : mysqldPaths) {
            File exe = new File(exePath);
            if (exe.exists() && exe.isFile()) {
                try {
                    System.out.println("[ConnectionFactory] Starting local MySQL server: " + exePath);
                    new ProcessBuilder(exePath)
                            .directory(exe.getParentFile())
                            .redirectErrorStream(true)
                            .start();
                    for (int i = 0; i < 12; i++) {
                        Thread.sleep(350);
                        if (isPortOpen(AppConfig.getDbHost(), AppConfig.getDbPort(), 400)) {
                            System.out.println("[ConnectionFactory] Local MySQL server is now accepting connections.");
                            return;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
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
        ensureMySqlRunning();
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

    private static class DriverShim implements Driver {
        private final Driver delegate;

        DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }
}
