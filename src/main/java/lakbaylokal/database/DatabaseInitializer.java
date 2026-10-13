package lakbaylokal.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static synchronized boolean initializeIfNeeded() {
        if (!ConnectionFactory.testConnection()) {
            return false;
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (!isSchemaPresent(conn)) {
                System.out.println("[DatabaseInitializer] Initializing schema and seed data for lakbay_lokal_db...");
                executeSqlScript(conn, "sql/01_schema.sql");
                executeSqlScript(conn, "sql/02_seed_data.sql");
                executeSqlScript(conn, "sql/03_views_procedures.sql");
                System.out.println("[DatabaseInitializer] Database initialized successfully.");
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Database initialization error: " + e.getMessage());
            return false;
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
                return false;
            }

            try (Statement stmt = conn.createStatement()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("#") || trimmed.isEmpty()) {
                        continue;
                    }
                    currentStatement.append(" ").append(line);
                    if (trimmed.endsWith(";")) {
                        String sql = currentStatement.toString().trim();
                        sql = sql.substring(0, sql.length() - 1).trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException ignored) {
                            }
                        }
                        currentStatement.setLength(0);
                    }
                }
            }
            return true;
        } catch (SQLException | IOException e) {
            return false;
        }
    }

    private static BufferedReader getScriptReader(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try {
                return new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ignored) {
            }
        }
        InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(path);
        if (is != null) {
            return new BufferedReader(new InputStreamReader(is));
        }
        return null;
    }
}
