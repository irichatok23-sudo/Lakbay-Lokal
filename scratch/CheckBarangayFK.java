import com.lakbaylokal.database.ConnectionFactory;
import java.sql.*;

public class CheckBarangayFK {
    public static void main(String[] args) throws Exception {
        try (Connection conn = ConnectionFactory.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getExportedKeys(conn.getCatalog(), null, "barangays");
            System.out.println("Foreign keys referencing barangays:");
            while (rs.next()) {
                String fkTable = rs.getString("FKTABLE_NAME");
                String fkCol = rs.getString("FKCOLUMN_NAME");
                String fkName = rs.getString("FK_NAME");
                int deleteRule = rs.getInt("DELETE_RULE");
                System.out.println("  " + fkTable + "." + fkCol + " (" + fkName + ") - deleteRule: " + deleteRule);
            }
        }
    }
}
