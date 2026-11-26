package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.StakeholderDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.AuditLog;
import com.lakbaylokal.model.StakeholderProfile;
import com.lakbaylokal.model.TourismService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StakeholderDAOImpl implements StakeholderDAO {

    private static final String PROFILE_SELECT =
            "SELECT sp.*, u.full_name AS owner_name, u.email, b.name AS barangay_name, b.municipality_id, m.name AS municipality_name " +
            "FROM stakeholder_profiles sp " +
            "JOIN users u ON sp.user_id = u.user_id " +
            "JOIN barangays b ON sp.barangay_id = b.barangay_id " +
            "JOIN municipalities m ON b.municipality_id = m.municipality_id ";

    @Override
    public StakeholderProfile findByUserId(int userId) {
        String sql = PROFILE_SELECT + "WHERE sp.user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProfile(rs);
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] findByUserId error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public StakeholderProfile findById(int profileId) {
        String sql = PROFILE_SELECT + "WHERE sp.profile_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProfile(rs);
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<StakeholderProfile> findAll(String category, Integer barangayId, String verificationStatus) {
        return findAll(category, barangayId, null, verificationStatus);
    }

    @Override
    public List<StakeholderProfile> findAll(String category, Integer barangayId, Integer municipalityId, String verificationStatus) {
        List<StakeholderProfile> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(PROFILE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND sp.category = ? ");
            params.add(category);
        }
        if (barangayId != null && barangayId > 0) {
            sql.append("AND sp.barangay_id = ? ");
            params.add(barangayId);
        }
        if (municipalityId != null && municipalityId > 0) {
            sql.append("AND b.municipality_id = ? ");
            params.add(municipalityId);
        }
        if (verificationStatus != null && !verificationStatus.isBlank() && !"ALL".equalsIgnoreCase(verificationStatus)) {
            if ("QUEUE".equalsIgnoreCase(verificationStatus)) {
                sql.append("AND sp.verification_status IN ('PENDING', 'UNDER_REVIEW', 'NEEDS_CORRECTION') ");
            } else if (verificationStatus.contains(",")) {
                String[] parts = verificationStatus.split(",");
                sql.append("AND sp.verification_status IN (");
                for (int i = 0; i < parts.length; i++) {
                    sql.append(i > 0 ? ",?" : "?");
                    params.add(parts[i].trim());
                }
                sql.append(") ");
            } else {
                sql.append("AND sp.verification_status = ? ");
                params.add(verificationStatus);
            }
        }

        sql.append("ORDER BY sp.profile_id ASC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProfile(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(StakeholderProfile p) {
        String sql = "INSERT INTO stakeholder_profiles (user_id, barangay_id, business_name, category, " +
                     "accreditation_no, contact_number, years_experience, verification_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId());
            ps.setInt(2, p.getBarangayId());
            ps.setString(3, p.getBusinessName());
            ps.setString(4, p.getCategory());
            ps.setString(5, p.getAccreditationNo());
            ps.setString(6, p.getContactNumber());
            ps.setInt(7, p.getYearsExperience());
            ps.setString(8, p.getVerificationStatus() != null ? p.getVerificationStatus() : "PENDING");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] create error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(StakeholderProfile p) {
        String sql = "UPDATE stakeholder_profiles SET barangay_id = ?, business_name = ?, category = ?, " +
                     "accreditation_no = ?, contact_number = ?, years_experience = ? WHERE profile_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getBarangayId());
            ps.setString(2, p.getBusinessName());
            ps.setString(3, p.getCategory());
            ps.setString(4, p.getAccreditationNo());
            ps.setString(5, p.getContactNumber());
            ps.setInt(6, p.getYearsExperience());
            ps.setInt(7, p.getProfileId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] update error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateVerificationStatus(int profileId, String status, int verifiedBy) {
        String sql = "UPDATE stakeholder_profiles SET verification_status = ?, verified_by = ?, verified_at = NOW() WHERE profile_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, verifiedBy);
            ps.setInt(3, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] updateVerificationStatus error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean resetToPending(int profileId) {
        String sql = "UPDATE stakeholder_profiles SET verification_status = 'PENDING', verified_by = NULL, verified_at = NULL WHERE profile_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] resetToPending error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atomically updates all correctable profile fields AND resets verification
     * status to PENDING, cleared verified_by and verified_at in a single SQL
     * UPDATE on one JDBC connection.
     *
     * Also inserts the STAKEHOLDER_RESUBMIT audit log entry within the same
     * transaction so that both the profile update and the audit trail either
     * succeed together or roll back together.
     *
     * @param profile the stakeholder profile with corrected field values set
     * @return true only if the profile row was actually updated
     */
    @Override
    public boolean resubmitProfile(StakeholderProfile profile) {
        return resubmitProfile(profile, null);
    }

    /**
     * Atomically updates all correctable profile fields AND resets verification
     * status to PENDING, cleared verified_by and verified_at in a single SQL
     * UPDATE on one JDBC connection, with custom audit log details participating
     * in the exact same transaction.
     *
     * @param profile the stakeholder profile with corrected field values set
     * @param auditLog optional specific audit log to insert; if null, defaults are used
     * @return true only if the profile row was actually updated and transaction committed
     */
    @Override
    public boolean resubmitProfile(StakeholderProfile profile, AuditLog auditLog) {
        String updateSql =
            "UPDATE stakeholder_profiles " +
            "SET business_name       = ?, " +
            "    accreditation_no    = ?, " +
            "    contact_number      = ?, " +
            "    years_experience    = ?, " +
            "    category            = ?, " +
            "    verification_status = 'PENDING', " +
            "    verified_by         = NULL, " +
            "    verified_at         = NULL " +
            "WHERE profile_id = ?";

        // The audit INSERT uses the exact schema of audit_logs
        String auditSql =
            "INSERT INTO audit_logs " +
            "(user_id, username, action, entity_name, entity_id, ip_address, details) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // ── 1. Atomic profile update ──────────────────────────────────────
            boolean updated;
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, profile.getBusinessName());
                ps.setString(2, profile.getAccreditationNo());
                ps.setString(3, profile.getContactNumber());
                ps.setInt   (4, profile.getYearsExperience());
                ps.setString(5, profile.getCategory());
                ps.setInt   (6, profile.getProfileId());
                updated = ps.executeUpdate() > 0;
            }

            if (!updated) {
                // Nothing was changed — the profile_id may not exist. Roll back cleanly.
                conn.rollback();
                System.err.println("[StakeholderDAO] resubmitProfile: no row matched profile_id=" + profile.getProfileId());
                return false;
            }

            // ── 2. Audit log within the exact same transaction ────────────────
            Integer userId = (auditLog != null && auditLog.getUserId() != null && auditLog.getUserId() > 0)
                    ? auditLog.getUserId()
                    : (profile.getUserId() > 0 ? profile.getUserId() : null);

            String username = (auditLog != null && auditLog.getUsername() != null && !auditLog.getUsername().isBlank())
                    ? auditLog.getUsername()
                    : (profile.getOwnerName() != null ? profile.getOwnerName() : "STAKEHOLDER");

            String action = (auditLog != null && auditLog.getAction() != null)
                    ? auditLog.getAction()
                    : "STAKEHOLDER_RESUBMIT";

            String entityName = (auditLog != null && auditLog.getEntityName() != null)
                    ? auditLog.getEntityName()
                    : "STAKEHOLDER_PROFILES";

            Integer entityId = (auditLog != null && auditLog.getEntityId() != null && auditLog.getEntityId() > 0)
                    ? auditLog.getEntityId()
                    : profile.getProfileId();

            String ipAddress = (auditLog != null && auditLog.getIpAddress() != null && !auditLog.getIpAddress().isBlank())
                    ? auditLog.getIpAddress()
                    : "127.0.0.1";

            String details = (auditLog != null && auditLog.getDetails() != null && !auditLog.getDetails().isBlank())
                    ? auditLog.getDetails()
                    : ("Resubmitted application: " + profile.getBusinessName() + " (profile_id=" + profile.getProfileId() + ")");

            try (PreparedStatement ps = conn.prepareStatement(auditSql)) {
                if (userId != null && userId > 0) ps.setInt(1, userId);
                else ps.setNull(1, java.sql.Types.INTEGER);
                ps.setString(2, username);
                ps.setString(3, action);
                ps.setString(4, entityName);
                if (entityId != null && entityId > 0) ps.setInt(5, entityId);
                else ps.setNull(5, java.sql.Types.INTEGER);
                ps.setString(6, ipAddress);
                ps.setString(7, details);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] resubmitProfile error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException re) {
                    System.err.println("[StakeholderDAO] rollback error: " + re.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<TourismService> findServicesByStakeholder(int stakeholderId) {
        List<TourismService> list = new ArrayList<>();
        String sql = "SELECT ts.*, sp.business_name, u.full_name AS stakeholder_name, b.name AS barangay_name " +
                     "FROM tourism_services ts " +
                     "JOIN stakeholder_profiles sp ON ts.stakeholder_id = sp.profile_id " +
                     "JOIN users u ON sp.user_id = u.user_id " +
                     "JOIN barangays b ON sp.barangay_id = b.barangay_id " +
                     "WHERE ts.stakeholder_id = ? ORDER BY ts.service_name ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stakeholderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapService(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] findServicesByStakeholder error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public TourismService findServiceById(int serviceId) {
        String sql = "SELECT ts.*, sp.business_name, u.full_name AS stakeholder_name, b.name AS barangay_name " +
                     "FROM tourism_services ts " +
                     "JOIN stakeholder_profiles sp ON ts.stakeholder_id = sp.profile_id " +
                     "JOIN users u ON sp.user_id = u.user_id " +
                     "JOIN barangays b ON sp.barangay_id = b.barangay_id " +
                     "WHERE ts.service_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapService(rs);
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] findServiceById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int createService(TourismService s) {
        String sql = "INSERT INTO tourism_services (stakeholder_id, service_name, service_category, description, " +
                     "base_price, pricing_unit, max_capacity, is_available) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getStakeholderId());
            ps.setString(2, s.getServiceName());
            ps.setString(3, s.getServiceCategory());
            ps.setString(4, s.getDescription());
            ps.setBigDecimal(5, s.getBasePrice());
            ps.setString(6, s.getPricingUnit());
            ps.setInt(7, s.getMaxCapacity());
            ps.setBoolean(8, s.isAvailable());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] createService error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean updateService(TourismService s) {
        String sql = "UPDATE tourism_services SET service_name = ?, service_category = ?, description = ?, " +
                     "base_price = ?, pricing_unit = ?, max_capacity = ?, is_available = ? WHERE service_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getServiceName());
            ps.setString(2, s.getServiceCategory());
            ps.setString(3, s.getDescription());
            ps.setBigDecimal(4, s.getBasePrice());
            ps.setString(5, s.getPricingUnit());
            ps.setInt(6, s.getMaxCapacity());
            ps.setBoolean(7, s.isAvailable());
            ps.setInt(8, s.getServiceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] updateService error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteService(int serviceId) {
        String sql = "DELETE FROM tourism_services WHERE service_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] deleteService error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<TourismService> findAllServices(String category, Boolean availableOnly) {
        List<TourismService> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT ts.*, sp.business_name, u.full_name AS stakeholder_name, b.name AS barangay_name " +
                "FROM tourism_services ts " +
                "JOIN stakeholder_profiles sp ON ts.stakeholder_id = sp.profile_id " +
                "JOIN users u ON sp.user_id = u.user_id " +
                "JOIN barangays b ON sp.barangay_id = b.barangay_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND ts.service_category = ? ");
            params.add(category);
        }
        if (availableOnly != null && availableOnly) {
            sql.append("AND ts.is_available = TRUE AND sp.verification_status = 'VERIFIED' ");
        }

        sql.append("ORDER BY ts.service_name ASC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapService(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StakeholderDAO] findAllServices error: " + e.getMessage());
        }
        return list;
    }

    private StakeholderProfile mapProfile(ResultSet rs) throws SQLException {
        StakeholderProfile p = new StakeholderProfile();
        p.setProfileId(rs.getInt("profile_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setBarangayId(rs.getInt("barangay_id"));
        p.setBusinessName(rs.getString("business_name"));
        p.setCategory(rs.getString("category"));
        p.setAccreditationNo(rs.getString("accreditation_no"));
        p.setContactNumber(rs.getString("contact_number"));
        p.setYearsExperience(rs.getInt("years_experience"));
        p.setVerificationStatus(rs.getString("verification_status"));
        int vBy = rs.getInt("verified_by");
        if (!rs.wasNull()) p.setVerifiedBy(vBy);
        p.setVerifiedAt(rs.getTimestamp("verified_at"));
        p.setOwnerName(rs.getString("owner_name"));
        p.setEmail(rs.getString("email"));
        p.setBarangayName(rs.getString("barangay_name"));
        p.setMunicipalityId(rs.getInt("municipality_id"));
        p.setMunicipalityName(rs.getString("municipality_name"));
        return p;
    }

    private TourismService mapService(ResultSet rs) throws SQLException {
        TourismService s = new TourismService();
        s.setServiceId(rs.getInt("service_id"));
        s.setStakeholderId(rs.getInt("stakeholder_id"));
        s.setServiceName(rs.getString("service_name"));
        s.setServiceCategory(rs.getString("service_category"));
        s.setDescription(rs.getString("description"));
        s.setBasePrice(rs.getBigDecimal("base_price"));
        s.setPricingUnit(rs.getString("pricing_unit"));
        s.setMaxCapacity(rs.getInt("max_capacity"));
        s.setAvailable(rs.getBoolean("is_available"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setBusinessName(rs.getString("business_name"));
        s.setStakeholderName(rs.getString("stakeholder_name"));
        s.setBarangayName(rs.getString("barangay_name"));
        return s;
    }
}
