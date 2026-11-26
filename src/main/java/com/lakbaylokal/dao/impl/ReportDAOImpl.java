package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.ReportDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.ArrivalRecord;

import java.sql.*;
import java.util.*;

public class ReportDAOImpl implements ReportDAO {

    @Override
    public List<Map<String, Object>> getDestinationFootfallSummaries() {
        return getDestinationFootfallSummaries(null);
    }

    @Override
    public List<Map<String, Object>> getDestinationFootfallSummaries(Integer municipalityId) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM v_destination_footfall_summary ");
        if (municipalityId != null && municipalityId > 0) {
            sql.append("WHERE municipality_name = (SELECT name FROM municipalities WHERE municipality_id = ?) ");
        }
        sql.append("ORDER BY grand_total_visitors DESC");
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (municipalityId != null && municipalityId > 0) {
                ps.setInt(1, municipalityId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getDestinationFootfallSummaries error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getStakeholderSummaries() {
        return getStakeholderSummaries(null);
    }

    @Override
    public List<Map<String, Object>> getStakeholderSummaries(Integer municipalityId) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM v_stakeholder_performance_summary ");
        if (municipalityId != null && municipalityId > 0) {
            sql.append("WHERE municipality_name = (SELECT name FROM municipalities WHERE municipality_id = ?) ");
        }
        sql.append("ORDER BY estimated_gross_income DESC");
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (municipalityId != null && municipalityId > 0) {
                ps.setInt(1, municipalityId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getStakeholderSummaries error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getBarangaySummaries() {
        return getBarangaySummaries(null);
    }

    @Override
    public List<Map<String, Object>> getBarangaySummaries(Integer municipalityId) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM v_barangay_tourism_overview ");
        if (municipalityId != null && municipalityId > 0) {
            sql.append("WHERE municipality_name = (SELECT name FROM municipalities WHERE municipality_id = ?) ");
        }
        sql.append("ORDER BY municipality_name ASC, barangay_name ASC");
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (municipalityId != null && municipalityId > 0) {
                ps.setInt(1, municipalityId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getBarangaySummaries error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ArrivalRecord> getArrivalLogs(Integer destinationId, String startDate, String endDate) {
        List<ArrivalRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT al.*, d.name AS destination_name, b.name AS barangay_name, u.full_name AS recorder_name " +
                "FROM tourism_arrival_logs al " +
                "JOIN destinations d ON al.destination_id = d.destination_id " +
                "JOIN barangays b ON d.barangay_id = b.barangay_id " +
                "JOIN users u ON al.recorded_by = u.user_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (destinationId != null && destinationId > 0) {
            sql.append("AND al.destination_id = ? ");
            params.add(destinationId);
        }
        if (startDate != null && !startDate.isBlank()) {
            sql.append("AND al.log_date >= ? ");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null && !endDate.isBlank()) {
            sql.append("AND al.log_date <= ? ");
            params.add(java.sql.Date.valueOf(endDate));
        }

        sql.append("ORDER BY al.log_date DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ArrivalRecord ar = new ArrivalRecord();
                    ar.setLogId(rs.getInt("log_id"));
                    ar.setDestinationId(rs.getInt("destination_id"));
                    ar.setLogDate(rs.getDate("log_date"));
                    ar.setLocalTouristsCount(rs.getInt("local_tourists_count"));
                    ar.setDomesticTouristsCount(rs.getInt("domestic_tourists_count"));
                    ar.setForeignTouristsCount(rs.getInt("foreign_tourists_count"));
                    ar.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    ar.setRecordedBy(rs.getInt("recorded_by"));
                    ar.setCreatedAt(rs.getTimestamp("created_at"));
                    ar.setDestinationName(rs.getString("destination_name"));
                    ar.setBarangayName(rs.getString("barangay_name"));
                    ar.setRecorderName(rs.getString("recorder_name"));
                    list.add(ar);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getArrivalLogs error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int recordArrival(ArrivalRecord ar) {
        String sql = "INSERT INTO tourism_arrival_logs (destination_id, log_date, local_tourists_count, " +
                     "domestic_tourists_count, foreign_tourists_count, total_revenue, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ar.getDestinationId());
            ps.setDate(2, ar.getLogDate());
            ps.setInt(3, ar.getLocalTouristsCount());
            ps.setInt(4, ar.getDomesticTouristsCount());
            ps.setInt(5, ar.getForeignTouristsCount());
            ps.setBigDecimal(6, ar.getTotalRevenue());
            ps.setInt(7, ar.getRecordedBy());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] recordArrival error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public Map<String, Object> getOverviewKPIs() {
        return getOverviewKPIs(null);
    }

    @Override
    public Map<String, Object> getOverviewKPIs(Integer municipalityId) {
        Map<String, Object> kpis = new LinkedHashMap<>();
        try (Connection conn = ConnectionFactory.getConnection()) {
            boolean scoped = municipalityId != null && municipalityId > 0;

            // Total Tourists (all-time visitors or global)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role_id = 6")) {
                if (rs.next()) kpis.put("totalTourists", rs.getInt(1));
            }

            // Active Bookings
            String activeBookingsSql = scoped
                ? "SELECT COUNT(DISTINCT b.booking_id) FROM bookings b JOIN booking_service_items bsi ON b.booking_id = bsi.booking_id JOIN stakeholder_profiles sp ON bsi.stakeholder_id = sp.profile_id JOIN barangays br ON sp.barangay_id = br.barangay_id WHERE br.municipality_id = ? AND b.status IN ('PENDING', 'ACCEPTED', 'CONFIRMED')"
                : "SELECT COUNT(*) FROM bookings WHERE status IN ('PENDING', 'ACCEPTED', 'CONFIRMED')";
            try (PreparedStatement ps = conn.prepareStatement(activeBookingsSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("activeBookings", rs.getInt(1));
                }
            }

            // Completed Bookings
            String compBookingsSql = scoped
                ? "SELECT COUNT(DISTINCT b.booking_id) FROM bookings b JOIN booking_service_items bsi ON b.booking_id = bsi.booking_id JOIN stakeholder_profiles sp ON bsi.stakeholder_id = sp.profile_id JOIN barangays br ON sp.barangay_id = br.barangay_id WHERE br.municipality_id = ? AND b.status = 'COMPLETED'"
                : "SELECT COUNT(*) FROM bookings WHERE status = 'COMPLETED'";
            try (PreparedStatement ps = conn.prepareStatement(compBookingsSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("completedBookings", rs.getInt(1));
                }
            }

            // Total Revenue
            String revSql = scoped
                ? "SELECT COALESCE(SUM(bsi.subtotal), 0) FROM booking_service_items bsi JOIN stakeholder_profiles sp ON bsi.stakeholder_id = sp.profile_id JOIN barangays br ON sp.barangay_id = br.barangay_id WHERE br.municipality_id = ? AND bsi.status IN ('ACCEPTED', 'COMPLETED')"
                : "SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE status = 'COMPLETED'";
            try (PreparedStatement ps = conn.prepareStatement(revSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("totalBookingRevenue", rs.getDouble(1));
                }
            }

            // Total Destinations
            String destSql = scoped
                ? "SELECT COUNT(*) FROM destinations d JOIN barangays br ON d.barangay_id = br.barangay_id WHERE br.municipality_id = ? AND d.status = 'ACTIVE'"
                : "SELECT COUNT(*) FROM destinations WHERE status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(destSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("totalDestinations", rs.getInt(1));
                }
            }

            // Verified Stakeholders
            String verStkSql = scoped
                ? "SELECT COUNT(*) FROM stakeholder_profiles sp JOIN barangays br ON sp.barangay_id = br.barangay_id WHERE br.municipality_id = ? AND sp.verification_status = 'VERIFIED'"
                : "SELECT COUNT(*) FROM stakeholder_profiles WHERE verification_status = 'VERIFIED'";
            try (PreparedStatement ps = conn.prepareStatement(verStkSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("verifiedStakeholders", rs.getInt(1));
                }
            }

            // Pending Stakeholder Verifications
            String pendStkSql = scoped
                ? "SELECT COUNT(*) FROM stakeholder_profiles sp JOIN barangays br ON sp.barangay_id = br.barangay_id WHERE br.municipality_id = ? AND sp.verification_status = 'PENDING'"
                : "SELECT COUNT(*) FROM stakeholder_profiles WHERE verification_status = 'PENDING'";
            try (PreparedStatement ps = conn.prepareStatement(pendStkSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("pendingStakeholders", rs.getInt(1));
                }
            }

            // Total Visitor Footfall (from logs)
            String footfallSql = scoped
                ? "SELECT COALESCE(SUM(al.local_tourists_count + al.domestic_tourists_count + al.foreign_tourists_count), 0) FROM tourism_arrival_logs al JOIN destinations d ON al.destination_id = d.destination_id JOIN barangays br ON d.barangay_id = br.barangay_id WHERE br.municipality_id = ?"
                : "SELECT COALESCE(SUM(local_tourists_count + domestic_tourists_count + foreign_tourists_count), 0) FROM tourism_arrival_logs";
            try (PreparedStatement ps = conn.prepareStatement(footfallSql)) {
                if (scoped) ps.setInt(1, municipalityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) kpis.put("totalVisitorFootfall", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getOverviewKPIs error: " + e.getMessage());
        }
        return kpis;
    }
}
