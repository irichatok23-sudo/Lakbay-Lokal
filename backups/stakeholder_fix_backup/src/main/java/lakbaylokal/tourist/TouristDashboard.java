package lakbaylokal.tourist;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;
import lakbaylokal.tourist.bookings.TouristBookingsPanel;
import lakbaylokal.tourist.destinations.TouristDestinationsPanel;
import lakbaylokal.tourist.itinerary.TouristItineraryPanel;
import lakbaylokal.tourist.packages.TouristPackagesPanel;
import lakbaylokal.tourist.reviews.TouristReviewsPanel;

import javax.swing.*;
import java.awt.*;

public class TouristDashboard extends JFrame {
    private final TouristController controller;
    private JTabbedPane tabbedPane;

    private TouristDestinationsPanel destinationsPanel;
    private TouristPackagesPanel packagesPanel;
    private TouristBookingsPanel bookingsPanel;
    private TouristItineraryPanel itineraryPanel;
    private TouristReviewsPanel reviewsPanel;

    public TouristDashboard() {
        this.controller = new TouristController();
        controller.setView(this);

        setTitle("Lakbay Lokal — Tourist Portal (Biliran Island Tourism)");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Header
        User currentUser = SessionManager.getCurrentUser();
        String name = (currentUser != null) ? currentUser.getFullName() : "Ana Sophia Cortez (Tourist)";
        root.add(UITheme.createHeaderBanner("Tourist Portal", name, controller::handleLogout), BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(UITheme.TEXT_DARK);

        destinationsPanel = new TouristDestinationsPanel(this);
        packagesPanel = new TouristPackagesPanel(this);
        bookingsPanel = new TouristBookingsPanel(this);
        itineraryPanel = new TouristItineraryPanel(this);
        reviewsPanel = new TouristReviewsPanel();

        tabbedPane.addTab("🌴 Explore Destinations", destinationsPanel);
        tabbedPane.addTab("🎒 Community Packages", packagesPanel);
        tabbedPane.addTab("📅 My Bookings", bookingsPanel);
        tabbedPane.addTab("🗺️ Custom Itinerary Planner", itineraryPanel);
        tabbedPane.addTab("⭐ Reviews & Ratings", reviewsPanel);

        // Auto-refresh when tab is clicked
        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            switch (selected) {
                case 0 -> destinationsPanel.loadData();
                case 1 -> packagesPanel.loadData();
                case 2 -> bookingsPanel.loadData();
                case 3 -> itineraryPanel.loadServices();
                case 4 -> reviewsPanel.loadMyReviews();
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    public void switchToTab(int index) {
        if (tabbedPane != null && index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public static void main(String[] args) {
        UITheme.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            TouristDashboard dashboard = new TouristDashboard();
            dashboard.setVisible(true);
        });
    }
}
