import java.sql.*;

public class CheckUsers {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/lakbay_lokal_db?useSSL=false", "root", "")) {
            String sql = "SELECT u.user_id, u.username, r.role_code, u.barangay_id, u.municipality_id, b.name as b_name, m.name as m_name " +
                         "FROM users u " +
                         "JOIN roles r ON u.role_id = r.role_id " +
                         "LEFT JOIN barangays b ON u.barangay_id = b.barangay_id " +
                         "LEFT JOIN municipalities m ON u.municipality_id = m.municipality_id";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                System.out.println("--- USERS & JURISDICTIONS ---");
                while (rs.next()) {
                    System.out.println(String.format("User: %-15s | Role: %-18s | BrgyId: %s (%s) | MuniId: %s (%s)",
                        rs.getString("username"),
                        rs.getString("role_code"),
                        rs.getString("barangay_id"),
                        rs.getString("b_name"),
                        rs.getString("municipality_id"),
                        rs.getString("m_name")
                    ));
                }
            }
        }
    }
}
