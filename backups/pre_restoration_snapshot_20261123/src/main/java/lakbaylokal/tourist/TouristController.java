package lakbaylokal.tourist;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.Booking;
import lakbaylokal.model.Destination;
import lakbaylokal.model.TourismPackage;
import lakbaylokal.model.User;
import lakbaylokal.service.BookingService;
import lakbaylokal.service.DestinationService;
import lakbaylokal.service.PackageService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TouristController {
    private final DestinationService destinationService;
    private final PackageService packageService;
    private final BookingService bookingService;
    private TouristDashboard view;

    public TouristController() {
        this.destinationService = new DestinationService();
        this.packageService = new PackageService();
        this.bookingService = new BookingService();
    }

    public void setView(TouristDashboard view) {
        this.view = view;
    }

    public List<Destination> getFeaturedDestinations() {
        return destinationService.getFeaturedDestinations();
    }

    public List<TourismPackage> getAllPackages() {
        return packageService.getAllPackages();
    }

    public List<Booking> getCurrentUserBookings() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            return bookingService.getBookingsByTourist(user.getUserId());
        }
        return bookingService.getAllBookings();
    }

    public boolean createBooking(int packageId, LocalDate date, int guests, BigDecimal amount) {
        User user = SessionManager.getCurrentUser();
        int touristId = (user != null) ? user.getUserId() : 1;

        Booking booking = new Booking();
        booking.setTouristId(touristId);
        booking.setPackageId(packageId);
        booking.setTravelDate(date);
        booking.setNumPax(guests);
        booking.setTotalAmount(amount);
        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");

        return bookingService.createBooking(booking);
    }

    public void handleLogout() {
        SessionManager.logout();
        if (view != null) {
            view.dispose();
        }
        // Launch login
        SwingUtilitiesLaunchLogin();
    }

    private void SwingUtilitiesLaunchLogin() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                Class<?> loginCls = Class.forName("lakbaylokal.authentication.LoginFrame");
                javax.swing.JFrame frame = (javax.swing.JFrame) loginCls.getDeclaredConstructor().newInstance();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
