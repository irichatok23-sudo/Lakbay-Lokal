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
            } else {
                syncSeedPasswords(conn);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Database initialization error: " + e.getMessage());
            return false;
        }
    }

    private static void syncSeedPasswords(Connection conn) {
        String validHash = "8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6";
        String legacyHash = "4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "INSERT INTO roles (role_id, role_code, role_name, description) VALUES " +
                "(1, 'SYSTEM_ADMIN', 'System Administrator', 'Full administrative authority across all modules, roles, security, and databases.') " +
                "ON DUPLICATE KEY UPDATE role_code = 'SYSTEM_ADMIN'"
            );
            stmt.executeUpdate(
                "INSERT IGNORE INTO users (user_id, username, password_hash, salt, email, full_name, phone, role_id, status) VALUES " +
                "(1, 'admin', '" + validHash + "', 'lakbaysalt2026', 'admin@lakbaylokal.gov.ph', 'Administrator Maria Santos', '+63 917 000 0001', 1, 'ACTIVE')"
            );
            stmt.executeUpdate(
                "UPDATE users SET password_hash = '" + validHash + "' " +
                "WHERE (password_hash = '" + legacyHash + "' OR password_hash IS NULL) " +
                "AND salt = 'lakbaysalt2026'"
            );
        } catch (SQLException ignored) {
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
