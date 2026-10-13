package lakbaylokal.database;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BackupRestoreService {

    public static File createBackup() throws Exception {
        File backupDir = new File("backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File backupFile = new File(backupDir, "lakbay_lokal_backup_" + timeStamp + ".sql");

        try (Connection conn = ConnectionFactory.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {

            writer.println("-- LAKBAY LOKAL DATABASE BACKUP DUMP");
            writer.println("-- Date: " + new Date());
            writer.println("SET FOREIGN_KEY_CHECKS = 0;");
            writer.println();

            DatabaseMetaData meta = conn.getMetaData();
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) tables.add(rs.getString("TABLE_NAME"));
            }

            try (Statement stmt = conn.createStatement()) {
                for (String table : tables) {
                    writer.println("-- Table: " + table);
                    try (ResultSet rs = stmt.executeQuery("SELECT * FROM `" + table + "`")) {
                        ResultSetMetaData rsMeta = rs.getMetaData();
                        int columnCount = rsMeta.getColumnCount();

                        while (rs.next()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("INSERT INTO `").append(table).append("` VALUES (");
                            for (int i = 1; i <= columnCount; i++) {
                                Object val = rs.getObject(i);
                                if (val == null) {
                                    sb.append("NULL");
                                } else if (val instanceof Number) {
                                    sb.append(val);
                                } else if (val instanceof Boolean b) {
                                    sb.append(b ? 1 : 0);
                                } else {
                                    String str = val.toString().replace("'", "\\'");
                                    sb.append("'").append(str).append("'");
                                }
                                if (i < columnCount) sb.append(", ");
                            }
                            sb.append(");");
                            writer.println(sb.toString());
                        }
                    }
                    writer.println();
                }
            }
            writer.println("SET FOREIGN_KEY_CHECKS = 1;");
        }
        return backupFile;
    }

    public static boolean restoreBackup(File backupFile) {
        if (!backupFile.exists()) return false;
        try (Connection conn = ConnectionFactory.getConnection()) {
            return DatabaseInitializer.executeSqlScript(conn, backupFile.getAbsolutePath());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean backupDatabase(String filename) {
        try {
            File f = createBackup();
            return f != null && f.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean restoreDatabase(String path) {
        if (path == null || path.isBlank()) return false;
        return restoreBackup(new File(path));
    }
}
