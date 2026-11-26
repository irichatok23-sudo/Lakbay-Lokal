package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.UserDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.Role;
import com.lakbaylokal.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private static final String SELECT_BASE = 
            "SELECT u.user_id, u.username, u.password_hash, u.salt, u.email, u.full_name, u.phone, " +
            "u.role_id, r.role_code, r.role_name, u.barangay_id, b.name AS barangay_name, " +
            "COALESCE(u.municipality_id, b.municipality_id) AS municipality_id, " +
            "COALESCE(mu.name, m.name) AS municipality_name, " +
            "u.status, u.created_at, u.updated_at " +
            "FROM users u " +
            "JOIN roles r ON u.role_id = r.role_id " +
            "LEFT JOIN barangays b ON u.barangay_id = b.barangay_id " +
            "LEFT JOIN municipalities m ON b.municipality_id = m.municipality_id " +
            "LEFT JOIN municipalities mu ON u.municipality_id = mu.municipality_id ";

    @Override
    public User findById(int userId) {
        String sql = SELECT_BASE + "WHERE u.user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs, true);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        String sql = SELECT_BASE + "WHERE u.username = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs, true);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findByUsername error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = SELECT_BASE + "WHERE u.email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs, true);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findByEmail error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY u.created_at DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs, false));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<User> findByRole(String roleCode) {
        return findFiltered(roleCode, null, null);
    }

    @Override
    public List<User> findFiltered(String roleCode, Integer municipalityId, Integer barangayId) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (roleCode != null && !roleCode.isBlank() && !"ALL".equalsIgnoreCase(roleCode)) {
            sql.append("AND r.role_code = ? ");
            params.add(roleCode);
        }
        if (municipalityId != null && municipalityId > 0) {
            sql.append("AND (u.municipality_id = ? OR b.municipality_id = ?) ");
            params.add(municipalityId);
            params.add(municipalityId);
        }
        if (barangayId != null && barangayId > 0) {
            sql.append("AND u.barangay_id = ? ");
            params.add(barangayId);
        }
        sql.append("ORDER BY u.created_at DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, false));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findFiltered error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, email, full_name, phone, role_id, barangay_id, municipality_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getFullName());
            ps.setString(6, user.getPhone());
            ps.setInt(7, user.getRoleId());
            if (user.getBarangayId() != null && user.getBarangayId() > 0) {
                ps.setInt(8, user.getBarangayId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            if (user.getMunicipalityId() != null && user.getMunicipalityId() > 0) {
                ps.setInt(9, user.getMunicipalityId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setString(10, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] create error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, role_id = ?, barangay_id = ?, municipality_id = ?, status = ? WHERE user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getRoleId());
            if (user.getBarangayId() != null && user.getBarangayId() > 0) {
                ps.setInt(4, user.getBarangayId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (user.getMunicipalityId() != null && user.getMunicipalityId() > 0) {
                ps.setInt(5, user.getMunicipalityId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, user.getStatus());
            ps.setInt(7, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] update error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePassword(int userId, String newHash, String newSalt) {
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateStatus error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Role> findAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT role_id, role_code, role_name, description FROM roles ORDER BY role_id ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new Role(
                        rs.getInt("role_id"),
                        rs.getString("role_code"),
                        rs.getString("role_name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findAllRoles error: " + e.getMessage());
        }
        return roles;
    }

    private User mapRow(ResultSet rs, boolean includeSecrets) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        if (includeSecrets) {
            u.setPasswordHash(rs.getString("password_hash"));
            u.setSalt(rs.getString("salt"));
        }
        u.setEmail(rs.getString("email"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleCode(rs.getString("role_code"));
        u.setRoleName(rs.getString("role_name"));
        int brgyId = rs.getInt("barangay_id");
        if (!rs.wasNull()) u.setBarangayId(brgyId);
        int munId = rs.getInt("municipality_id");
        if (!rs.wasNull()) u.setMunicipalityId(munId);
        u.setBarangayName(rs.getString("barangay_name"));
        u.setMunicipalityName(rs.getString("municipality_name"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}
