package lakbaylokal.dao.impl;

import lakbaylokal.dao.ReviewDAO;
import lakbaylokal.database.ConnectionFactory;
import lakbaylokal.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAOImpl implements ReviewDAO {

    private static final String SELECT_BASE =
            "SELECT r.*, u.full_name AS tourist_name " +
            "FROM reviews r " +
            "JOIN tourist_profiles tp ON r.tourist_id = tp.profile_id " +
            "JOIN users u ON tp.user_id = u.user_id ";

    @Override
    public List<Review> findByTarget(String targetType, int targetId) {
        List<Review> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE r.target_type = ? AND r.target_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetType);
            ps.setInt(2, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapReview(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Review> findByBookingId(int bookingId) {
        List<Review> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE r.booking_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapReview(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY r.created_at DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapReview(rs));
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public int create(Review r) {
        String sql = "INSERT INTO reviews (booking_id, tourist_id, target_type, target_id, rating, comment) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getBookingId());
            ps.setInt(2, r.getTouristId());
            ps.setString(3, r.getTargetType());
            ps.setInt(4, r.getTargetId());
            ps.setInt(5, r.getRating());
            ps.setString(6, r.getComment());

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
    public boolean delete(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ignored) { return false; }
    }

    @Override
    public boolean hasUserReviewed(int touristId, int bookingId, String targetType, int targetId) {
        String sql = "SELECT review_id FROM reviews WHERE tourist_id = ? AND booking_id = ? AND target_type = ? AND target_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, touristId);
            ps.setInt(2, bookingId);
            ps.setString(3, targetType);
            ps.setInt(4, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) { return false; }
    }

    private Review mapReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setBookingId(rs.getInt("booking_id"));
        r.setTouristId(rs.getInt("tourist_id"));
        r.setTargetType(rs.getString("target_type"));
        r.setTargetId(rs.getInt("target_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setTouristName(rs.getString("tourist_name"));
        return r;
    }
}
