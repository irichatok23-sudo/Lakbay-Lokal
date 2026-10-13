package com.lakbaylokal.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes database tables and seed data if not already present.
 */
public class DatabaseInitializer {

    public static synchronized boolean initializeIfNeeded() {
        if (!ConnectionFactory.testConnection()) {
            System.err.println("[DatabaseInitializer] Cannot initialize database: connection unreachable.");
            return false;
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (!isSchemaPresent(conn)) {
                System.out.println("[DatabaseInitializer] Tables not detected. Initializing schema and seed data...");
                executeSqlScript(conn, "sql/01_schema.sql");
                executeSqlScript(conn, "sql/02_seed_data.sql");
                executeSqlScript(conn, "sql/03_views_procedures.sql");
                System.out.println("[DatabaseInitializer] Database initialized successfully.");
            } else {
                System.out.println("[DatabaseInitializer] Existing database schema verified.");
            }
            copyAssetsIfNeeded();
            return true;
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Database initialization error: " + e.getMessage());
            return false;
        }
    }

    private static void copyAssetsIfNeeded() {
        try {
            File targetDir = new File("src/main/resources/web/assets");
            if (!targetDir.exists()) targetDir.mkdirs();
            File binTargetDir = new File("bin/web/assets");
            if (!binTargetDir.exists()) binTargetDir.mkdirs();
        } catch (Exception ignored) {
        }
    }

    private static boolean isSchemaPresent(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "users", new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean executeSqlScript(Connection conn, String relativePathOrResource) {
        StringBuilder currentStatement = new StringBuilder();
        try (BufferedReader reader = getScriptReader(relativePathOrResource)) {
            if (reader == null) {
                System.err.println("[DatabaseInitializer] Script file not found: " + relativePathOrResource);
                return false;
            }

            try (Statement stmt = conn.createStatement()) {
                String line;
                boolean inDelimiter = false;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("#") || trimmed.isEmpty()) {
                        continue;
                    }

                    currentStatement.append(" ").append(line);

                    if (trimmed.endsWith(";")) {
                        String sql = currentStatement.toString().trim();
                        // Remove trailing semicolon
                        sql = sql.substring(0, sql.length() - 1).trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException e) {
                                // Ignore duplicate key errors during seeding
                                if (e.getErrorCode() != 1062 && e.getErrorCode() != 1050) {
                                    System.err.println("[DatabaseInitializer] Warning executing statement: " + e.getMessage());
                                }
                            }
                        }
                        currentStatement.setLength(0);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Failed to execute script: " + relativePathOrResource + " - " + e.getMessage());
            return false;
        }
    }

    private static BufferedReader getScriptReader(String path) {
        // First check workspace file system
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try {
                return new BufferedReader(new FileReader(file));
            } catch (Exception ignored) {
            }
        }

        // Fallback to classpath
        InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(path);
        if (is != null) {
            return new BufferedReader(new InputStreamReader(is));
        }

        return null;
    }
}
