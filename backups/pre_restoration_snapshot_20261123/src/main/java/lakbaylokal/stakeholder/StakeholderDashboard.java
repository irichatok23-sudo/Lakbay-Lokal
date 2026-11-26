package lakbaylokal.stakeholder;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;
import lakbaylokal.stakeholder.bookings.StakeholderBookingsPanel;
import lakbaylokal.stakeholder.profile.StakeholderProfilePanel;
import lakbaylokal.stakeholder.schedules.StakeholderSchedulesPanel;
import lakbaylokal.stakeholder.services.StakeholderServicesPanel;

import javax.swing.*;
import java.awt.*;

public class StakeholderDashboard extends JFrame {
    private final StakeholderController controller;
    private JTabbedPane tabbedPane;

    private StakeholderProfilePanel profilePanel;
    private StakeholderServicesPanel servicesPanel;
    private StakeholderSchedulesPanel schedulesPanel;
    private StakeholderBookingsPanel bookingsPanel;

    public StakeholderDashboard() {
        this.controller = new StakeholderController();
        controller.setView(this);

        setTitle("Lakbay Lokal — Stakeholder Enterprise Portal (Biliran Tourism)");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        User currentUser = SessionManager.getCurrentUser();
        String name = (currentUser != null) ? currentUser.getFullName() : "Capitan Danilo Flores (Stakeholder)";
        root.add(UITheme.createHeaderBanner("Local Tourism Stakeholder", name, controller::handleLogout), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(UITheme.TEXT_DARK);

        profilePanel = new StakeholderProfilePanel();
        servicesPanel = new StakeholderServicesPanel();
        schedulesPanel = new StakeholderSchedulesPanel();
        bookingsPanel = new StakeholderBookingsPanel();

        tabbedPane.addTab("🏢 Enterprise Profile & Verification", profilePanel);
        tabbedPane.addTab("🛎️ Services & Offerings", servicesPanel);
        tabbedPane.addTab("⏰ Trip & Tour Schedules", schedulesPanel);
        tabbedPane.addTab("📋 Client Reservations & Orders", bookingsPanel);

        // Auto-refresh when tab is clicked
        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            switch (selected) {
                case 0 -> profilePanel.loadProfileData();
                case 1 -> servicesPanel.loadServices();
                case 3 -> bookingsPanel.loadBookings();
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    public static void main(String[] args) {
        UITheme.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            StakeholderDashboard frame = new StakeholderDashboard();
            frame.setVisible(true);
        });
    }
}
