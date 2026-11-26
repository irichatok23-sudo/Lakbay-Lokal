import java.sql.*;

public class CheckCreatedDestinations {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/lakbay_lokal_db?useSSL=false", "root", "")) {
            String sql = "SELECT * FROM audit_logs WHERE action IN ('CREATE_DESTINATION', 'UPDATE_DESTINATION', 'DELETE_DESTINATION')";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                System.out.println("--- DESTINATION AUDIT LOGS ---");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println(String.format("[%s] User: %s | Action: %s | EntityID: %s | Details: %s",
                        rs.getTimestamp("timestamp"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("entity_id"),
                        rs.getString("details")
                    ));
                }
                System.out.println("Total destination audit entries: " + count);
            }
        }
    }
}
