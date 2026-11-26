import java.sql.*;

public class CheckDestinations {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/lakbay_lokal_db?useSSL=false", "root", "")) {
            String sql = "SELECT d.destination_id, d.name, d.status, d.validated_by, d.barangay_id, b.name as barangay_name, m.name as municipality_name " +
                         "FROM destinations d " +
                         "LEFT JOIN barangays b ON d.barangay_id = b.barangay_id " +
                         "LEFT JOIN municipalities m ON b.municipality_id = m.municipality_id";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                System.out.println("--- ALL DESTINATIONS IN DATABASE ---");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println(String.format("ID: %d | Name: %s | Status: %s | ValidatedBy: %s | BrgyId: %s | Brgy: %s | Muni: %s",
                        rs.getInt("destination_id"),
                        rs.getString("name"),
                        rs.getString("status"),
                        rs.getString("validated_by"),
                        rs.getString("barangay_id"),
                        rs.getString("barangay_name"),
                        rs.getString("municipality_name")
                    ));
                }
                System.out.println("Total destinations in database: " + count);
            }
        }
    }
}
