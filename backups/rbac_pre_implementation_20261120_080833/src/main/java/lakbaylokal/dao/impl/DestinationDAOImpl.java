package lakbaylokal.dao.impl;

import lakbaylokal.dao.DestinationDAO;
import lakbaylokal.database.ConnectionFactory;
import lakbaylokal.model.Barangay;
import lakbaylokal.model.Destination;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationDAOImpl implements DestinationDAO {

    private static final String SELECT_BASE =
            "SELECT d.destination_id, d.barangay_id, d.name, d.category, d.description, d.entrance_fee, " +
            "d.operating_hours, d.location_coordinates, d.contact_info, d.image_url, d.status, " +
            "d.validated_by, d.validated_at, d.created_at, b.name AS barangay_name, m.name AS municipality_name, " +
            "COALESCE(ROUND(AVG(r.rating), 1), 5.0) AS avg_rating, COUNT(r.review_id) AS review_count " +
            "FROM destinations d " +
            "JOIN barangays b ON d.barangay_id = b.barangay_id " +
            "JOIN municipalities m ON b.municipality_id = m.municipality_id " +
            "LEFT JOIN reviews r ON r.target_type = 'DESTINATION' AND r.target_id = d.destination_id ";

    @Override
    public Destination findById(int destinationId) {
        String sql = SELECT_BASE + "WHERE d.destination_id = ? GROUP BY d.destination_id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ignored) {}
        return null;
    }

    @Override
    public List<Destination> findAll(String category, Integer barangayId, String status) {
        List<Destination> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND d.category = ? ");
            params.add(category);
        }
        if (barangayId != null && barangayId > 0) {
            sql.append("AND d.barangay_id = ? ");
            params.add(barangayId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND d.status = ? ");
            params.add(status);
        }

        sql.append("GROUP BY d.destination_id ORDER BY avg_rating DESC, d.name ASC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public int create(Destination d) {
        String sql = "INSERT INTO destinations (barangay_id, name, category, description, entrance_fee, " +
                     "operating_hours, location_coordinates, contact_info, image_url, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getBarangayId());
            ps.setString(2, d.getName());
            ps.setString(3, d.getCategory());
            ps.setString(4, d.getDescription());
            ps.setBigDecimal(5, d.getEntranceFee());
            ps.setString(6, d.getOperatingHours());
            ps.setString(7, d.getLocationCoordinates());
            ps.setString(8, d.getContactInfo());
            ps.setString(9, d.getImageUrl());
            ps.setString(10, d.getStatus() != null ? d.getStatus() : "ACTIVE");

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
    public boolean update(Destination d) {
        String sql = "UPDATE destinations SET barangay_id = ?, name = ?, category = ?, description = ?, " +
                     "entrance_fee = ?, operating_hours = ?, location_coordinates = ?, contact_info = ?, " +
                     "image_url = ?, status = ? WHERE destination_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getBarangayId());
            ps.setString(2, d.getName());
            ps.setString(3, d.getCategory());
            ps.setString(4, d.getDescription());
            ps.setBigDecimal(5, d.getEntranceFee());
            ps.setString(6, d.getOperatingHours());
            ps.setString(7, d.getLocationCoordinates());
            ps.setString(8, d.getContactInfo());
            ps.setString(9, d.getImageUrl());
            ps.setString(10, d.getStatus());
            ps.setInt(11, d.getDestinationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ignored) { return false; }
    }

    @Override
    public boolean updateStatus(int destinationId, String status) {
        String sql = "UPDATE destinations SET status = ? WHERE destination_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, destinationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ignored) { return false; }
    }

    @Override
    public boolean validateDestination(int destinationId, int validatorUserId) {
        String sql = "UPDATE destinations SET validated_by = ?, validated_at = NOW(), status = 'ACTIVE' WHERE destination_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, validatorUserId);
            ps.setInt(2, destinationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ignored) { return false; }
    }

    @Override
    public boolean delete(int destinationId) {
        String sql = "DELETE FROM destinations WHERE destination_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ignored) { return false; }
    }

    @Override
    public List<Barangay> findAllBarangays() {
        List<Barangay> list = new ArrayList<>();
        String sql = "SELECT b.barangay_id, b.municipality_id, m.name AS municipality_name, b.name, " +
                     "b.contact_person, b.contact_number " +
                     "FROM barangays b JOIN municipalities m ON b.municipality_id = m.municipality_id " +
                     "ORDER BY m.name ASC, b.name ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Barangay b = new Barangay();
                b.setBarangayId(rs.getInt("barangay_id"));
                b.setMunicipalityId(rs.getInt("municipality_id"));
                b.setMunicipalityName(rs.getString("municipality_name"));
                b.setName(rs.getString("name"));
                b.setContactPerson(rs.getString("contact_person"));
                b.setContactNumber(rs.getString("contact_number"));
                list.add(b);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    private Destination mapRow(ResultSet rs) throws SQLException {
        Destination d = new Destination();
        d.setDestinationId(rs.getInt("destination_id"));
        d.setBarangayId(rs.getInt("barangay_id"));
        d.setName(rs.getString("name"));
        d.setCategory(rs.getString("category"));
        d.setDescription(rs.getString("description"));
        d.setEntranceFee(rs.getBigDecimal("entrance_fee"));
        d.setOperatingHours(rs.getString("operating_hours"));
        d.setLocationCoordinates(rs.getString("location_coordinates"));
        d.setContactInfo(rs.getString("contact_info"));
        d.setImageUrl(rs.getString("image_url"));
        d.setStatus(rs.getString("status"));
        int valBy = rs.getInt("validated_by");
        if (!rs.wasNull()) d.setValidatedBy(valBy);
        d.setValidatedAt(rs.getTimestamp("validated_at"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        d.setBarangayName(rs.getString("barangay_name"));
        d.setMunicipalityName(rs.getString("municipality_name"));
        d.setAverageRating(rs.getDouble("avg_rating"));
        d.setReviewCount(rs.getInt("review_count"));
        return d;
    }
}
