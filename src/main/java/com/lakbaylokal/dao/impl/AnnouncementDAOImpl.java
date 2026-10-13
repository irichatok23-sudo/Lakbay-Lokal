package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.AnnouncementDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.Announcement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAOImpl implements AnnouncementDAO {

    private static final String SELECT_BASE =
            "SELECT a.*, u.full_name AS publisher_name, b.name AS target_barangay_name " +
            "FROM announcements a " +
            "JOIN users u ON a.published_by = u.user_id " +
            "LEFT JOIN barangays b ON a.target_barangay_id = b.barangay_id ";

    @Override
    public Announcement findById(int announcementId) {
        String sql = SELECT_BASE + "WHERE a.announcement_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, announcementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAnnouncement(rs);
            }
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Announcement> findAll(String category, Integer barangayId, String status) {
        List<Announcement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND a.category = ? ");
            params.add(category);
        }
        if (barangayId != null && barangayId > 0) {
            sql.append("AND (a.target_barangay_id = ? OR a.target_barangay_id IS NULL) ");
            params.add(barangayId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND a.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY a.publish_date DESC, a.announcement_id DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAnnouncement(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(Announcement a) {
        String sql = "INSERT INTO announcements (title, content, category, target_barangay_id, published_by, " +
                     "publish_date, expiry_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory());
            if (a.getTargetBarangayId() != null && a.getTargetBarangayId() > 0) ps.setInt(4, a.getTargetBarangayId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, a.getPublishedBy());
            ps.setDate(6, a.getPublishDate());
            ps.setDate(7, a.getExpiryDate());
            ps.setString(8, a.getStatus() != null ? a.getStatus() : "PUBLISHED");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] create error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(Announcement a) {
        String sql = "UPDATE announcements SET title = ?, content = ?, category = ?, target_barangay_id = ?, " +
                     "publish_date = ?, expiry_date = ?, status = ? WHERE announcement_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory());
            if (a.getTargetBarangayId() != null && a.getTargetBarangayId() > 0) ps.setInt(4, a.getTargetBarangayId());
            else ps.setNull(4, Types.INTEGER);
            ps.setDate(5, a.getPublishDate());
            ps.setDate(6, a.getExpiryDate());
            ps.setString(7, a.getStatus());
            ps.setInt(8, a.getAnnouncementId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] update error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int announcementId) {
        String sql = "DELETE FROM announcements WHERE announcement_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, announcementId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private Announcement mapAnnouncement(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setAnnouncementId(rs.getInt("announcement_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setCategory(rs.getString("category"));
        int tbId = rs.getInt("target_barangay_id");
        if (!rs.wasNull()) a.setTargetBarangayId(tbId);
        a.setPublishedBy(rs.getInt("published_by"));
        a.setPublishDate(rs.getDate("publish_date"));
        a.setExpiryDate(rs.getDate("expiry_date"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setPublisherName(rs.getString("publisher_name"));
        a.setTargetBarangayName(rs.getString("target_barangay_name"));
        return a;
    }
}
