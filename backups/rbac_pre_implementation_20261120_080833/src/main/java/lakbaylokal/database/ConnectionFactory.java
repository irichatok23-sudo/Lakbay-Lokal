package lakbaylokal.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

public class ConnectionFactory {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final Properties properties = new Properties();
    private static volatile boolean mysqlWakeAttempted = false;

    static {
        loadJdbcDriver();

        try (InputStream is = ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException ignored) {
        }
    }

    private static void loadJdbcDriver() {
        try {
            Class.forName(DRIVER_CLASS);
            return;
        } catch (ClassNotFoundException ignored) {
        }

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
                    return;
                } catch (Exception ignored) {
                }
            }
        }
        System.err.println("[ConnectionFactory] MySQL JDBC Driver not found in classpath.");
    }

    private static synchronized void ensureMySqlRunning() {
        if (isPortOpen(getHost(), getPort(), 400)) {
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
                    new ProcessBuilder(exePath)
                            .directory(exe.getParentFile())
                            .redirectErrorStream(true)
                            .start();
                    for (int i = 0; i < 12; i++) {
                        Thread.sleep(350);
                        if (isPortOpen(getHost(), getPort(), 400)) {
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

    public static String getHost() { return properties.getProperty("db.host", "localhost"); }
    public static int getPort() {
        try { return Integer.parseInt(properties.getProperty("db.port", "3306")); }
        catch (NumberFormatException e) { return 3306; }
    }
    public static String getDbName() { return properties.getProperty("db.name", "lakbay_lokal_db"); }
    public static String getUser() { return properties.getProperty("db.user", "root"); }
    public static String getPassword() { return properties.getProperty("db.password", ""); }

    public static String getJdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila&characterEncoding=utf8",
                getHost(), getPort(), getDbName()
        );
    }

    public static Connection getConnection() throws SQLException {
        ensureMySqlRunning();
        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try { resource.close(); } catch (Exception ignored) {}
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
