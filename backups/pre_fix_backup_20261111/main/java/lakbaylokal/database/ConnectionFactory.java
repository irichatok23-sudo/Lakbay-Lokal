package lakbaylokal.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final Properties properties = new Properties();

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("[ConnectionFactory] MySQL JDBC Driver not found in classpath.");
        }

        try (InputStream is = ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException ignored) {
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
}
