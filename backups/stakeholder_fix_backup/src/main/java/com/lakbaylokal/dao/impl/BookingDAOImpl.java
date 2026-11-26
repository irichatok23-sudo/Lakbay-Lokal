package com.lakbaylokal.dao.impl;

import com.lakbaylokal.dao.BookingDAO;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.model.Booking;
import com.lakbaylokal.model.BookingItem;
import com.lakbaylokal.model.TouristProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl implements BookingDAO {

    private static final String SELECT_BASE =
            "SELECT b.*, u.full_name AS tourist_name, u.email AS tourist_email, u.phone AS tourist_phone, " +
            "p.name AS package_name " +
            "FROM bookings b " +
            "JOIN tourist_profiles tp ON b.tourist_id = tp.profile_id " +
            "JOIN users u ON tp.user_id = u.user_id " +
            "LEFT JOIN tourism_packages p ON b.package_id = p.package_id ";

    @Override
    public Booking findById(int bookingId) {
        String sql = SELECT_BASE + "WHERE b.booking_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking b = mapBooking(rs);
                    b.setItems(findItemsForBooking(conn, bookingId));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Booking findByReference(String reference) {
        String sql = SELECT_BASE + "WHERE b.booking_reference = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reference);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking b = mapBooking(rs);
                    b.setItems(findItemsForBooking(conn, b.getBookingId()));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findByReference error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public BookingItem findItemById(int itemId) {
        String sql = "SELECT bsi.*, ts.service_name, sp.business_name, u.full_name AS stakeholder_name " +
                     "FROM booking_service_items bsi " +
                     "JOIN tourism_services ts ON bsi.service_id = ts.service_id " +
                     "JOIN stakeholder_profiles sp ON bsi.stakeholder_id = sp.profile_id " +
                     "JOIN users u ON sp.user_id = u.user_id " +
                     "WHERE bsi.item_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookingItem item = new BookingItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setServiceId(rs.getInt("service_id"));
                    item.setStakeholderId(rs.getInt("stakeholder_id"));
                    item.setScheduledTime(rs.getTimestamp("scheduled_time"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setStatus(rs.getString("status"));
                    item.setServiceName(rs.getString("service_name"));
                    item.setBusinessName(rs.getString("business_name"));
                    item.setStakeholderName(rs.getString("stakeholder_name"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findItemById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Booking> findByTouristId(int touristId) {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE b.tourist_id = ? ORDER BY b.created_at DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, touristId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapBooking(rs);
                    b.setItems(findItemsForBooking(conn, b.getBookingId()));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findByTouristId error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Booking> findByStakeholderId(int stakeholderId) {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_BASE +
                     "WHERE b.booking_id IN (SELECT DISTINCT booking_id FROM booking_service_items WHERE stakeholder_id = ?) " +
                     "ORDER BY b.created_at DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stakeholderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapBooking(rs);
                    b.setItems(findItemsForBooking(conn, b.getBookingId()));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findByStakeholderId error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Booking> findAll(String status) {
        List<Booking> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND b.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY b.created_at DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapBooking(rs);
                    b.setItems(findItemsForBooking(conn, b.getBookingId()));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(Booking b, List<BookingItem> items) {
        String sqlBooking = "INSERT INTO bookings (booking_reference, tourist_id, package_id, travel_date_start, " +
                            "travel_date_end, number_of_pax, total_amount, status, custom_notes) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, b.getBookingReference());
                ps.setInt(2, b.getTouristId());
                if (b.getPackageId() != null && b.getPackageId() > 0) ps.setInt(3, b.getPackageId());
                else ps.setNull(3, Types.INTEGER);
                ps.setDate(4, b.getTravelDateStart());
                ps.setDate(5, b.getTravelDateEnd());
                ps.setInt(6, b.getNumberOfPax());
                ps.setBigDecimal(7, b.getTotalAmount());
                ps.setString(8, b.getStatus() != null ? b.getStatus() : "PENDING");
                ps.setString(9, b.getCustomNotes());

                int affected = ps.executeUpdate();
                int newBookingId = -1;
                if (affected > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) newBookingId = keys.getInt(1);
                    }
                }

                if (newBookingId > 0 && items != null && !items.isEmpty()) {
                    String sqlItem = "INSERT INTO booking_service_items (booking_id, service_id, stakeholder_id, " +
                                     "scheduled_time, unit_price, quantity, subtotal, status) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                        for (BookingItem item : items) {
                            psItem.setInt(1, newBookingId);
                            psItem.setInt(2, item.getServiceId());
                            psItem.setInt(3, item.getStakeholderId());
                            if (item.getScheduledTime() != null) psItem.setTimestamp(4, item.getScheduledTime());
                            else psItem.setNull(4, Types.TIMESTAMP);
                            psItem.setBigDecimal(5, item.getUnitPrice());
                            psItem.setInt(6, item.getQuantity());
                            psItem.setBigDecimal(7, item.getSubtotal());
                            psItem.setString(8, item.getStatus() != null ? item.getStatus() : "PENDING");
                            psItem.addBatch();
                        }
                        psItem.executeBatch();
                    }
                }

                if (newBookingId > 0) {
                    conn.commit();
                    return newBookingId;
                } else {
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[BookingDAO] create transaction error: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] create error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean updateStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookingDAO] updateStatus error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateItemStatus(int itemId, String status) {
        String sql = "UPDATE booking_service_items SET status = ? WHERE item_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookingDAO] updateItemStatus error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public TouristProfile findTouristProfileByUserId(int userId) {
        String sql = "SELECT tp.*, u.full_name, u.email, u.phone " +
                     "FROM tourist_profiles tp JOIN users u ON tp.user_id = u.user_id " +
                     "WHERE tp.user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TouristProfile p = new TouristProfile();
                    p.setProfileId(rs.getInt("profile_id"));
                    p.setUserId(rs.getInt("user_id"));
                    p.setOriginCity(rs.getString("origin_city"));
                    p.setNationality(rs.getString("nationality"));
                    p.setEmergencyContact(rs.getString("emergency_contact"));
                    p.setEmergencyPhone(rs.getString("emergency_phone"));
                    p.setTravelPreferences(rs.getString("travel_preferences"));
                    p.setFullName(rs.getString("full_name"));
                    p.setEmail(rs.getString("email"));
                    p.setPhone(rs.getString("phone"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] findTouristProfileByUserId error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int createTouristProfile(TouristProfile p) {
        String sql = "INSERT INTO tourist_profiles (user_id, origin_city, nationality, emergency_contact, emergency_phone, travel_preferences) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId());
            ps.setString(2, p.getOriginCity());
            ps.setString(3, p.getNationality() != null ? p.getNationality() : "Filipino");
            ps.setString(4, p.getEmergencyContact());
            ps.setString(5, p.getEmergencyPhone());
            ps.setString(6, p.getTravelPreferences());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] createTouristProfile error: " + e.getMessage());
        }
        return -1;
    }

    private List<BookingItem> findItemsForBooking(Connection conn, int bookingId) throws SQLException {
        List<BookingItem> list = new ArrayList<>();
        String sql = "SELECT bsi.*, ts.service_name, sp.business_name, u.full_name AS stakeholder_name " +
                     "FROM booking_service_items bsi " +
                     "JOIN tourism_services ts ON bsi.service_id = ts.service_id " +
                     "JOIN stakeholder_profiles sp ON bsi.stakeholder_id = sp.profile_id " +
                     "JOIN users u ON sp.user_id = u.user_id " +
                     "WHERE bsi.booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingItem item = new BookingItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setServiceId(rs.getInt("service_id"));
                    item.setStakeholderId(rs.getInt("stakeholder_id"));
                    item.setScheduledTime(rs.getTimestamp("scheduled_time"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setStatus(rs.getString("status"));
                    item.setServiceName(rs.getString("service_name"));
                    item.setBusinessName(rs.getString("business_name"));
                    item.setStakeholderName(rs.getString("stakeholder_name"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setBookingReference(rs.getString("booking_reference"));
        b.setTouristId(rs.getInt("tourist_id"));
        int pkgId = rs.getInt("package_id");
        if (!rs.wasNull()) b.setPackageId(pkgId);
        b.setBookingDate(rs.getTimestamp("booking_date"));
        b.setTravelDateStart(rs.getDate("travel_date_start"));
        b.setTravelDateEnd(rs.getDate("travel_date_end"));
        b.setNumberOfPax(rs.getInt("number_of_pax"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setStatus(rs.getString("status"));
        b.setCustomNotes(rs.getString("custom_notes"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        b.setUpdatedAt(rs.getTimestamp("updated_at"));
        b.setTouristName(rs.getString("tourist_name"));
        b.setTouristEmail(rs.getString("tourist_email"));
        b.setTouristPhone(rs.getString("tourist_phone"));
        b.setPackageName(rs.getString("package_name"));
        return b;
    }
}
