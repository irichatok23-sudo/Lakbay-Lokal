package com.lakbaylokal.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
                syncSeedPasswords(conn);
                ensureMunicipalitySchema(conn);
                ensureVerificationStatuses(conn);
                ensureAnnouncementSchema(conn);
            }
            copyAssetsIfNeeded();
            return true;
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Database initialization error: " + e.getMessage());
            return false;
        }
    }

    private static void syncSeedPasswords(Connection conn) {
        String validHash = "8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6";
        String legacyHash = "4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853";
        try (Statement stmt = conn.createStatement()) {
            // Ensure SYSTEM_ADMIN role exists
            stmt.executeUpdate(
                "INSERT INTO roles (role_id, role_code, role_name, description) VALUES " +
                "(1, 'SYSTEM_ADMIN', 'System Administrator', 'Full administrative authority across all modules, roles, security, and databases.') " +
                "ON DUPLICATE KEY UPDATE role_code = 'SYSTEM_ADMIN'"
            );

            // Ensure default admin account exists without overwriting existing user customizations
            stmt.executeUpdate(
                "INSERT IGNORE INTO users (user_id, username, password_hash, salt, email, full_name, phone, role_id, status) VALUES " +
                "(1, 'admin', '" + validHash + "', 'lakbaysalt2026', 'admin@lakbaylokal.gov.ph', 'Administrator Maria Santos', '+63 917 000 0001', 1, 'ACTIVE')"
            );

            // Fix accounts with legacy hash or NULL
            int updated = stmt.executeUpdate(
                "UPDATE users SET password_hash = '" + validHash + "' " +
                "WHERE (password_hash = '" + legacyHash + "' OR password_hash IS NULL) " +
                "AND salt = 'lakbaysalt2026'"
            );

            // Fix accounts with broken/plain-text password_hash (not a valid SHA-256 or PBKDF2 hash)
            // A valid SHA-256 hex is exactly 64 chars; PBKDF2 starts with 'pbkdf2$'
            int fixed = stmt.executeUpdate(
                "UPDATE users SET password_hash = '" + validHash + "' " +
                "WHERE salt = 'lakbaysalt2026' " +
                "AND password_hash NOT LIKE 'pbkdf2%' " +
                "AND password_hash != '" + validHash + "' " +
                "AND (LENGTH(TRIM(password_hash)) != 64 OR password_hash LIKE '%\\r%' OR password_hash LIKE '%\\n%')"
            );

            int totalFixed = updated + fixed;
            if (totalFixed > 0) {
                System.out.println("[DatabaseInitializer] Synchronized " + totalFixed + " seed user account(s) to verified SHA-256 password hash.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Notice during password sync: " + e.getMessage());
        }
    }

    private static void ensureMunicipalitySchema(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            boolean hasCol = false;
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "users", "municipality_id")) {
                if (rs.next()) {
                    hasCol = true;
                }
            }

            try (Statement stmt = conn.createStatement()) {
                if (!hasCol) {
                    System.out.println("[DatabaseInitializer] Migrating users schema: Adding municipality_id...");
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN municipality_id INT NULL AFTER barangay_id");
                    try {
                        stmt.executeUpdate("ALTER TABLE users ADD CONSTRAINT fk_users_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id) ON DELETE SET NULL ON UPDATE CASCADE");
                    } catch (Exception ignored) {}
                }

                stmt.executeUpdate(
                    "UPDATE users u JOIN barangays b ON u.barangay_id = b.barangay_id " +
                    "SET u.municipality_id = b.municipality_id " +
                    "WHERE u.municipality_id IS NULL"
                );
                stmt.executeUpdate("UPDATE users SET municipality_id = 1 WHERE username = 'lgu_naval'");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Notice during municipality schema check: " + e.getMessage());
        }
    }

    private static void ensureVerificationStatuses(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "ALTER TABLE stakeholder_profiles MODIFY COLUMN verification_status " +
                "ENUM('PENDING', 'UNDER_REVIEW', 'NEEDS_CORRECTION', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING'"
            );
            stmt.executeUpdate(
                "ALTER TABLE verification_records MODIFY COLUMN status " +
                "ENUM('PENDING', 'APPROVED', 'REJECTED', 'REQUEST_CORRECTION') NOT NULL"
            );
            System.out.println("[DatabaseInitializer] Verification statuses verified (PENDING, UNDER_REVIEW, NEEDS_CORRECTION, VERIFIED, REJECTED).");
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Notice during verification status migration: " + e.getMessage());
        }
    }

    private static void ensureAnnouncementSchema(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            boolean hasImageUrl = false;
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "announcements", "image_url")) {
                if (rs.next()) {
                    hasImageUrl = true;
                }
            }

            try (Statement stmt = conn.createStatement()) {
                if (!hasImageUrl) {
                    stmt.executeUpdate("ALTER TABLE announcements ADD COLUMN image_url VARCHAR(255) NULL AFTER target_barangay_id");
                }
                stmt.executeUpdate(
                    "ALTER TABLE announcements MODIFY COLUMN category " +
                    "ENUM('ANNOUNCEMENT', 'EVENT', 'FESTIVAL', 'TRAVEL_GUIDELINE', 'DESTINATION_CLOSURE', 'ADVISORY') NOT NULL"
                );
                stmt.executeUpdate(
                    "ALTER TABLE announcements MODIFY COLUMN status " +
                    "ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT'"
                );
                stmt.executeUpdate("ALTER TABLE announcements MODIFY COLUMN publish_date DATE NULL");
                System.out.println("[DatabaseInitializer] Announcements schema verified (DRAFT/PUBLISHED/ARCHIVED, ADVISORY/DESTINATION_CLOSURE, image_url).");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Notice during announcements schema check: " + e.getMessage());
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
        } catch (SQLException | IOException e) {
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
            } catch (java.io.IOException ignored) {
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
