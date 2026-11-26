package lakbaylokal.dao;

import lakbaylokal.model.Booking;
import lakbaylokal.model.BookingItem;
import lakbaylokal.model.TouristProfile;

import java.util.List;

public interface BookingDAO {
    Booking findById(int bookingId);
    Booking findByReference(String reference);
    List<Booking> findByTouristId(int touristId);
    List<Booking> findByStakeholderId(int stakeholderId);
    List<Booking> findAll(String status);
    int create(Booking booking, List<BookingItem> items);
    boolean updateStatus(int bookingId, String status);
    boolean updateItemStatus(int itemId, String status);

    TouristProfile findTouristProfileByUserId(int userId);
    int createTouristProfile(TouristProfile profile);
}
