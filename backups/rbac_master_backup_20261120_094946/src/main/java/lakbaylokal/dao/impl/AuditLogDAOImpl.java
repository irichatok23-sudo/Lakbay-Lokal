package lakbaylokal.dao.impl;

import lakbaylokal.dao.AuditLogDAO;
import lakbaylokal.database.ConnectionFactory;
import lakbaylokal.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    @Override
    public int log(AuditLog l) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, entity_name, entity_id, ip_address, details) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (l.getUserId() != null && l.getUserId() > 0) ps.setInt(1, l.getUserId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, l.getUsername() != null ? l.getUsername() : "ANONYMOUS");
            ps.setString(3, l.getAction());
            ps.setString(4, l.getEntityName());
            if (l.getEntityId() != null && l.getEntityId() > 0) ps.setInt(5, l.getEntityId());
            else ps.setNull(5, Types.INTEGER);
            ps.setString(6, l.getIpAddress() != null ? l.getIpAddress() : "127.0.0.1");
            ps.setString(7, l.getDetails());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException ignored) {}
        return -1;
    }

    @Override
    public List<AuditLog> findAll(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAuditLog(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<AuditLog> findByUser(int userId, int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAuditLog(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    private AuditLog mapAuditLog(ResultSet rs) throws SQLException {
        AuditLog l = new AuditLog();
        l.setLogId(rs.getInt("log_id"));
        int uId = rs.getInt("user_id");
        if (!rs.wasNull()) l.setUserId(uId);
        l.setUsername(rs.getString("username"));
        l.setAction(rs.getString("action"));
        l.setEntityName(rs.getString("entity_name"));
        int entId = rs.getInt("entity_id");
        if (!rs.wasNull()) l.setEntityId(entId);
        l.setIpAddress(rs.getString("ip_address"));
        l.setDetails(rs.getString("details"));
        l.setTimestamp(rs.getTimestamp("timestamp"));
        return l;
    }
}
