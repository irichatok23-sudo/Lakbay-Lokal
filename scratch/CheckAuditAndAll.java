import java.sql.*;

public class CheckAuditAndAll {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/lakbay_lokal_db?useSSL=false", "root", "")) {
            System.out.println("--- CHECKING ALL TABLES IN DATABASE ---");
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    System.out.println("Table: " + rs.getString("TABLE_NAME"));
                }
            }

            System.out.println("\n--- RECENT AUDIT LOGS ---");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM audit_logs ORDER BY log_id DESC LIMIT 30")) {
                while (rs.next()) {
                    System.out.println(String.format("[%s] User: %s | Action: %s | Entity: %s | ID: %s | Details: %s",
                        rs.getTimestamp("timestamp"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("entity_name"),
                        rs.getString("entity_id"),
                        rs.getString("details")
                    ));
                }
            }

            System.out.println("\n--- CHECKING DATABASES ON MYSQL SERVER ---");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SHOW DATABASES")) {
                while (rs.next()) {
                    System.out.println("DB: " + rs.getString(1));
                }
            }
        }
    }
}
