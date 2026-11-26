package com.lakbaylokal.service;

import com.lakbaylokal.dao.BookingDAO;
import com.lakbaylokal.dao.impl.BookingDAOImpl;
import com.lakbaylokal.model.Booking;
import com.lakbaylokal.model.BookingItem;
import com.lakbaylokal.model.TouristProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private static final Random RANDOM = new Random();

    public String generateReference() {
        String year = new SimpleDateFormat("yyyy").format(new Date());
        int num = 1000 + RANDOM.nextInt(9000);
        return "LL-" + year + "-" + num;
    }

    public int createBooking(Booking booking, List<BookingItem> items) {
        if (booking.getBookingReference() == null || booking.getBookingReference().isBlank()) {
            booking.setBookingReference(generateReference());
        }
        return bookingDAO.create(booking, items);
    }

    public Booking getBookingById(int bookingId) {
        return bookingDAO.findById(bookingId);
    }

    public Booking getBookingByReference(String ref) {
        return bookingDAO.findByReference(ref);
    }

    public List<Booking> getBookingsByTourist(int touristId) {
        return bookingDAO.findByTouristId(touristId);
    }

    public List<Booking> getBookingsByStakeholder(int stakeholderId) {
        return bookingDAO.findByStakeholderId(stakeholderId);
    }

    public List<Booking> getAllBookings(String status) {
        return bookingDAO.findAll(status);
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        return bookingDAO.updateStatus(bookingId, status);
    }

    public boolean updateItemStatus(int itemId, String status) {
        return bookingDAO.updateItemStatus(itemId, status);
    }

    public TouristProfile getTouristProfileByUserId(int userId) {
        return bookingDAO.findTouristProfileByUserId(userId);
    }
}
