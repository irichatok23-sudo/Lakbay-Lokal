package lakbaylokal.dao.impl;

import lakbaylokal.dao.ReportDAO;
import lakbaylokal.database.ConnectionFactory;
import lakbaylokal.model.ArrivalRecord;

import java.sql.*;
import java.util.*;

public class ReportDAOImpl implements ReportDAO {

    @Override
    public List<Map<String, Object>> getDestinationFootfallSummaries() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM v_destination_footfall_summary ORDER BY grand_total_visitors DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Map<String, Object>> getStakeholderSummaries() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM v_stakeholder_performance_summary ORDER BY estimated_gross_income DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Map<String, Object>> getBarangaySummaries() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM v_barangay_tourism_overview ORDER BY municipality_name ASC, barangay_name ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
        return -1;
    }

    @Override
    public Map<String, Object> getOverviewKPIs() {
        Map<String, Object> kpis = new LinkedHashMap<>();
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role_id = 6")) {
                if (rs.next()) kpis.put("totalTourists", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bookings WHERE status IN ('PENDING', 'ACCEPTED', 'CONFIRMED')")) {
                if (rs.next()) kpis.put("activeBookings", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bookings WHERE status = 'COMPLETED'")) {
                if (rs.next()) kpis.put("completedBookings", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE status = 'COMPLETED'")) {
                if (rs.next()) kpis.put("totalBookingRevenue", rs.getDouble(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM destinations WHERE status = 'ACTIVE'")) {
                if (rs.next()) kpis.put("totalDestinations", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM stakeholder_profiles WHERE verification_status = 'VERIFIED'")) {
                if (rs.next()) kpis.put("verifiedStakeholders", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM stakeholder_profiles WHERE verification_status = 'PENDING'")) {
                if (rs.next()) kpis.put("pendingStakeholders", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(local_tourists_count + domestic_tourists_count + foreign_tourists_count), 0) FROM tourism_arrival_logs")) {
                if (rs.next()) kpis.put("totalVisitorFootfall", rs.getInt(1));
            }
        } catch (SQLException ignored) {}
        return kpis;
    }
}
