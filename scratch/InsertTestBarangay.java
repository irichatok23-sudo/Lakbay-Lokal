package scratch;

import com.lakbaylokal.database.ConnectionFactory;
import java.sql.*;

public class InsertTestBarangay {
    public static void main(String[] args) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            // First clean up any old test barangay
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM barangays WHERE name = 'Brgy Test Automated'")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO barangays (municipality_id, name, contact_person, contact_number) VALUES (1, 'Brgy Test Automated', 'Hon. Test Official', '09990001111')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("INSERTED_ID:" + rs.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
