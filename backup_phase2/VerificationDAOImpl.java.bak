package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.VerificationDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.VerificationRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VerificationDAOImpl implements VerificationDAO {

    private static final String SELECT_BASE =
            "SELECT vr.*, sp.business_name, u_stk.full_name AS stakeholder_name, b.name AS barangay_name, " +
            "u_rev.full_name AS reviewer_name " +
            "FROM verification_records vr " +
            "JOIN stakeholder_profiles sp ON vr.stakeholder_id = sp.profile_id " +
            "JOIN users u_stk ON sp.user_id = u_stk.user_id " +
            "JOIN barangays b ON vr.barangay_id = b.barangay_id " +
            "JOIN users u_rev ON vr.reviewer_id = u_rev.user_id ";

    @Override
    public VerificationRecord findById(int verificationId) {
        String sql = SELECT_BASE + "WHERE vr.verification_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, verificationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("[VerificationDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<VerificationRecord> findByStakeholderId(int stakeholderId) {
        List<VerificationRecord> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE vr.stakeholder_id = ? ORDER BY vr.decision_date DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stakeholderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VerificationDAO] findByStakeholderId error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<VerificationRecord> findAll(Integer barangayId, String status) {
        return findAll(barangayId, null, status);
    }

    @Override
    public List<VerificationRecord> findAll(Integer barangayId, Integer municipalityId, String status) {
        List<VerificationRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (barangayId != null && barangayId > 0) {
            sql.append("AND vr.barangay_id = ? ");
            params.add(barangayId);
        }
        if (municipalityId != null && municipalityId > 0) {
            sql.append("AND b.municipality_id = ? ");
            params.add(municipalityId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND vr.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY vr.decision_date DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VerificationDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(VerificationRecord r) {
        String sql = "INSERT INTO verification_records (stakeholder_id, barangay_id, reviewer_id, status, " +
                     "requirements_submitted, remarks) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getStakeholderId());
            ps.setInt(2, r.getBarangayId());
            ps.setInt(3, r.getReviewerId());
            ps.setString(4, r.getStatus());
            ps.setString(5, r.getRequirementsSubmitted());
            ps.setString(6, r.getRemarks());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VerificationDAO] create error: " + e.getMessage());
        }
        return -1;
    }

    private VerificationRecord mapRecord(ResultSet rs) throws SQLException {
        VerificationRecord r = new VerificationRecord();
        r.setVerificationId(rs.getInt("verification_id"));
        r.setStakeholderId(rs.getInt("stakeholder_id"));
        r.setBarangayId(rs.getInt("barangay_id"));
        r.setReviewerId(rs.getInt("reviewer_id"));
        r.setStatus(rs.getString("status"));
        r.setRequirementsSubmitted(rs.getString("requirements_submitted"));
        r.setRemarks(rs.getString("remarks"));
        r.setDecisionDate(rs.getTimestamp("decision_date"));
        r.setBusinessName(rs.getString("business_name"));
        r.setStakeholderName(rs.getString("stakeholder_name"));
        r.setBarangayName(rs.getString("barangay_name"));
        r.setReviewerName(rs.getString("reviewer_name"));
        return r;
    }
}
