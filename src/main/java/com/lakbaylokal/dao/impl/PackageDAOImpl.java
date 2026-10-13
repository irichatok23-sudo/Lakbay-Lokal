package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.PackageDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.Destination;
import com.lakbaylokal.model.TourismPackage;
import com.lakbaylokal.model.TourismService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAOImpl implements PackageDAO {

    @Override
    public TourismPackage findById(int packageId) {
        String sql = "SELECT p.*, COALESCE(ROUND(AVG(r.rating), 1), 5.0) AS avg_rating, COUNT(r.review_id) AS review_count " +
                     "FROM tourism_packages p " +
                     "LEFT JOIN reviews r ON r.target_type = 'PACKAGE' AND r.target_id = p.package_id " +
                     "WHERE p.package_id = ? " +
                     "GROUP BY p.package_id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TourismPackage p = mapRow(rs);
                    p.setDestinations(findDestinationsForPackage(conn, packageId));
                    p.setServices(findServicesForPackage(conn, packageId));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("[PackageDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<TourismPackage> findAll(String type, String status) {
        List<TourismPackage> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, COALESCE(ROUND(AVG(r.rating), 1), 5.0) AS avg_rating, COUNT(r.review_id) AS review_count " +
                "FROM tourism_packages p " +
                "LEFT JOIN reviews r ON r.target_type = 'PACKAGE' AND r.target_id = p.package_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            sql.append("AND p.package_type = ? ");
            params.add(type);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND p.status = ? ");
            params.add(status);
        }

        sql.append("GROUP BY p.package_id ORDER BY avg_rating DESC, p.package_id ASC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TourismPackage p = mapRow(rs);
                    p.setDestinations(findDestinationsForPackage(conn, p.getPackageId()));
                    p.setServices(findServicesForPackage(conn, p.getPackageId()));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PackageDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds) {
        String sql = "INSERT INTO tourism_packages (name, description, package_type, price_per_pax, duration_days, " +
                     "min_pax, max_pax, inclusions, exclusions, status, image_url, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pkg.getName());
                ps.setString(2, pkg.getDescription());
                ps.setString(3, pkg.getPackageType() != null ? pkg.getPackageType() : "PRE_DESIGNED");
                ps.setBigDecimal(4, pkg.getPricePerPax());
                ps.setInt(5, pkg.getDurationDays());
                ps.setInt(6, pkg.getMinPax());
                ps.setInt(7, pkg.getMaxPax());
                ps.setString(8, pkg.getInclusions());
                ps.setString(9, pkg.getExclusions());
                ps.setString(10, pkg.getStatus() != null ? pkg.getStatus() : "ACTIVE");
                ps.setString(11, pkg.getImageUrl());
                if (pkg.getCreatedBy() != null) ps.setInt(12, pkg.getCreatedBy());
                else ps.setNull(12, Types.INTEGER);

                int affected = ps.executeUpdate();
                int newPkgId = -1;
                if (affected > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) newPkgId = keys.getInt(1);
                    }
                }

                if (newPkgId > 0) {
                    insertJunctionRecords(conn, newPkgId, destinationIds, serviceIds);
                    conn.commit();
                    return newPkgId;
                } else {
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[PackageDAO] create transaction error: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[PackageDAO] create error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(TourismPackage pkg, List<Integer> destinationIds, List<Integer> serviceIds) {
        String sql = "UPDATE tourism_packages SET name = ?, description = ?, package_type = ?, price_per_pax = ?, " +
                     "duration_days = ?, min_pax = ?, max_pax = ?, inclusions = ?, exclusions = ?, " +
                     "status = ?, image_url = ? WHERE package_id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pkg.getName());
                ps.setString(2, pkg.getDescription());
                ps.setString(3, pkg.getPackageType());
                ps.setBigDecimal(4, pkg.getPricePerPax());
                ps.setInt(5, pkg.getDurationDays());
                ps.setInt(6, pkg.getMinPax());
                ps.setInt(7, pkg.getMaxPax());
                ps.setString(8, pkg.getInclusions());
                ps.setString(9, pkg.getExclusions());
                ps.setString(10, pkg.getStatus());
                ps.setString(11, pkg.getImageUrl());
                ps.setInt(12, pkg.getPackageId());

                boolean ok = ps.executeUpdate() > 0;
                if (ok) {
                    try (PreparedStatement delDest = conn.prepareStatement("DELETE FROM package_destinations WHERE package_id = ?");
                         PreparedStatement delSvc = conn.prepareStatement("DELETE FROM package_services WHERE package_id = ?")) {
                        delDest.setInt(1, pkg.getPackageId());
                        delDest.executeUpdate();
                        delSvc.setInt(1, pkg.getPackageId());
                        delSvc.executeUpdate();
                    }
                    insertJunctionRecords(conn, pkg.getPackageId(), destinationIds, serviceIds);
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[PackageDAO] update transaction error: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[PackageDAO] update error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int packageId) {
        String sql = "DELETE FROM tourism_packages WHERE package_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PackageDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private void insertJunctionRecords(Connection conn, int packageId, List<Integer> destinationIds, List<Integer> serviceIds) throws SQLException {
        if (destinationIds != null && !destinationIds.isEmpty()) {
            String sqlDest = "INSERT INTO package_destinations (package_id, destination_id, visit_order) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlDest)) {
                int order = 1;
                for (Integer destId : destinationIds) {
                    ps.setInt(1, packageId);
                    ps.setInt(2, destId);
                    ps.setInt(3, order++);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
        if (serviceIds != null && !serviceIds.isEmpty()) {
            String sqlSvc = "INSERT INTO package_services (package_id, service_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlSvc)) {
                for (Integer svcId : serviceIds) {
                    ps.setInt(1, packageId);
                    ps.setInt(2, svcId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private List<Destination> findDestinationsForPackage(Connection conn, int packageId) throws SQLException {
        List<Destination> list = new ArrayList<>();
        String sql = "SELECT d.*, b.name AS barangay_name, m.name AS municipality_name " +
                     "FROM package_destinations pd " +
                     "JOIN destinations d ON pd.destination_id = d.destination_id " +
                     "JOIN barangays b ON d.barangay_id = b.barangay_id " +
                     "JOIN municipalities m ON b.municipality_id = m.municipality_id " +
                     "WHERE pd.package_id = ? ORDER BY pd.visit_order ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Destination d = new Destination();
                    d.setDestinationId(rs.getInt("destination_id"));
                    d.setName(rs.getString("name"));
                    d.setCategory(rs.getString("category"));
                    d.setBarangayName(rs.getString("barangay_name"));
                    d.setMunicipalityName(rs.getString("municipality_name"));
                    d.setEntranceFee(rs.getBigDecimal("entrance_fee"));
                    d.setImageUrl(rs.getString("image_url"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    private List<TourismService> findServicesForPackage(Connection conn, int packageId) throws SQLException {
        List<TourismService> list = new ArrayList<>();
        String sql = "SELECT ts.*, sp.business_name, u.full_name AS stakeholder_name " +
                     "FROM package_services ps " +
                     "JOIN tourism_services ts ON ps.service_id = ts.service_id " +
                     "JOIN stakeholder_profiles sp ON ts.stakeholder_id = sp.profile_id " +
                     "JOIN users u ON sp.user_id = u.user_id " +
                     "WHERE ps.package_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TourismService s = new TourismService();
                    s.setServiceId(rs.getInt("service_id"));
                    s.setServiceName(rs.getString("service_name"));
                    s.setServiceCategory(rs.getString("service_category"));
                    s.setBasePrice(rs.getBigDecimal("base_price"));
                    s.setPricingUnit(rs.getString("pricing_unit"));
                    s.setBusinessName(rs.getString("business_name"));
                    s.setStakeholderName(rs.getString("stakeholder_name"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    private TourismPackage mapRow(ResultSet rs) throws SQLException {
        TourismPackage p = new TourismPackage();
        p.setPackageId(rs.getInt("package_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPackageType(rs.getString("package_type"));
        p.setPricePerPax(rs.getBigDecimal("price_per_pax"));
        p.setDurationDays(rs.getInt("duration_days"));
        p.setMinPax(rs.getInt("min_pax"));
        p.setMaxPax(rs.getInt("max_pax"));
        p.setInclusions(rs.getString("inclusions"));
        p.setExclusions(rs.getString("exclusions"));
        p.setStatus(rs.getString("status"));
        p.setImageUrl(rs.getString("image_url"));
        int cBy = rs.getInt("created_by");
        if (!rs.wasNull()) p.setCreatedBy(cBy);
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setAverageRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        return p;
    }
}
