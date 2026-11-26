package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.AnnouncementDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.Announcement;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementDAOImpl implements AnnouncementDAO {

    private static final String SELECT_BASE =
            "SELECT a.*, u.full_name AS publisher_name, b.name AS target_barangay_name, " +
            "m.municipality_id, m.name AS municipality_name " +
            "FROM announcements a " +
            "JOIN users u ON a.published_by = u.user_id " +
            "LEFT JOIN barangays b ON a.target_barangay_id = b.barangay_id " +
            "LEFT JOIN municipalities m ON b.municipality_id = m.municipality_id ";

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
        return findFiltered(category, barangayId, null, status, null, false);
    }

    @Override
    public List<Announcement> findFiltered(String category, Integer barangayId, Integer municipalityId,
                                          String status, String search, boolean excludeExpired) {
        List<Announcement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND a.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND a.category = ? ");
            params.add(category.trim().toUpperCase());
        }

        if (barangayId != null && barangayId > 0) {
            sql.append("AND (a.target_barangay_id = ? OR a.target_barangay_id IS NULL) ");
            params.add(barangayId);
        } else if (municipalityId != null && municipalityId > 0) {
            sql.append("AND (b.municipality_id = ? OR a.target_barangay_id IS NULL) ");
            params.add(municipalityId);
        }

        if (search != null && !search.isBlank()) {
            sql.append("AND (a.title LIKE ? OR a.content LIKE ?) ");
            String term = "%" + search.trim() + "%";
            params.add(term);
            params.add(term);
        }

        if (excludeExpired) {
            // Public / Tourist view: exclude expired and future-dated scheduled publications
            sql.append("AND (a.expiry_date IS NULL OR a.expiry_date >= CURRENT_DATE()) ");
            sql.append("AND (a.publish_date IS NULL OR a.publish_date <= CURRENT_DATE()) ");
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
            System.err.println("[AnnouncementDAO] findFiltered error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(Announcement a) {
        String sql = "INSERT INTO announcements (title, content, category, target_barangay_id, image_url, published_by, " +
                     "publish_date, expiry_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory());
            if (a.getTargetBarangayId() != null && a.getTargetBarangayId() > 0) ps.setInt(4, a.getTargetBarangayId());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, a.getImageUrl());
            ps.setInt(6, a.getPublishedBy());
            ps.setDate(7, a.getPublishDate());
            ps.setDate(8, a.getExpiryDate());
            ps.setString(9, a.getStatus() != null ? a.getStatus() : "DRAFT");

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
                     "image_url = ?, publish_date = ?, expiry_date = ?, status = ? WHERE announcement_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory());
            if (a.getTargetBarangayId() != null && a.getTargetBarangayId() > 0) ps.setInt(4, a.getTargetBarangayId());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, a.getImageUrl());
            ps.setDate(6, a.getPublishDate());
            ps.setDate(7, a.getExpiryDate());
            ps.setString(8, a.getStatus());
            ps.setInt(9, a.getAnnouncementId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] update error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(int announcementId, String status, Date publishDate) {
        String sql = publishDate != null
                ? "UPDATE announcements SET status = ?, publish_date = ? WHERE announcement_id = ?"
                : "UPDATE announcements SET status = ? WHERE announcement_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (publishDate != null) {
                ps.setDate(2, publishDate);
                ps.setInt(3, announcementId);
            } else {
                ps.setInt(2, announcementId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] updateStatus error: " + e.getMessage());
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

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        int total = 0, published = 0, draft = 0, archived = 0;
        Map<String, Integer> byCategory = new LinkedHashMap<>();

        String sql = "SELECT status, category, COUNT(*) as cnt FROM announcements GROUP BY status, category";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String st = rs.getString("status");
                String cat = rs.getString("category");
                int c = rs.getInt("cnt");
                total += c;
                if ("PUBLISHED".equalsIgnoreCase(st)) published += c;
                else if ("DRAFT".equalsIgnoreCase(st)) draft += c;
                else if ("ARCHIVED".equalsIgnoreCase(st)) archived += c;

                byCategory.put(cat, byCategory.getOrDefault(cat, 0) + c);
            }
        } catch (SQLException e) {
            System.err.println("[AnnouncementDAO] getStatistics error: " + e.getMessage());
        }

        stats.put("total", total);
        stats.put("published", published);
        stats.put("draft", draft);
        stats.put("archived", archived);
        stats.put("byCategory", byCategory);
        return stats;
    }

    private Announcement mapAnnouncement(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setAnnouncementId(rs.getInt("announcement_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setCategory(rs.getString("category"));
        int tbId = rs.getInt("target_barangay_id");
        if (!rs.wasNull()) a.setTargetBarangayId(tbId);
        try { a.setImageUrl(rs.getString("image_url")); } catch (SQLException ignored) {}
        a.setPublishedBy(rs.getInt("published_by"));
        a.setPublishDate(rs.getDate("publish_date"));
        a.setExpiryDate(rs.getDate("expiry_date"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setPublisherName(rs.getString("publisher_name"));
        a.setTargetBarangayName(rs.getString("target_barangay_name"));
        try {
            int mId = rs.getInt("municipality_id");
            if (!rs.wasNull()) a.setMunicipalityId(mId);
            a.setMunicipalityName(rs.getString("municipality_name"));
        } catch (SQLException ignored) {}
        return a;
    }
}
