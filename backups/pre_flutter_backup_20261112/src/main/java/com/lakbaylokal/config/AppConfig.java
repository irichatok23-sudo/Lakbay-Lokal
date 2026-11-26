package com.lakbaylokal.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application and database configuration from db.properties
 * or environment variables with resilient fallback defaults.
 */
public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                System.out.println("[AppConfig] db.properties not found in classpath. Using default properties.");
            }
        } catch (Exception e) {
            System.err.println("[AppConfig] Warning: Failed to load db.properties: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replace('.', '_').toUpperCase());
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(val.trim());
    }

    // Convenience Getters
    public static String getDbHost() { return get("db.host", "localhost"); }
    public static int getDbPort() { return getInt("db.port", 3306); }
    public static String getDbName() { return get("db.name", "lakbay_lokal_db"); }
    public static String getDbUser() { return get("db.user", "root"); }
    public static String getDbPassword() { return get("db.password", ""); }
    public static int getServerPort() { return getInt("server.port", 8080); }
    public static String getServerHost() { return get("server.host", "localhost"); }
    public static String getAppName() { return get("app.name", "Lakbay Lokal"); }
    public static String getAppVersion() { return get("app.version", "1.0.0"); }
}
