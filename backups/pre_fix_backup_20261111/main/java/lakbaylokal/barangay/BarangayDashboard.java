package lakbaylokal.barangay;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.barangay.destinations.BarangayDestinationsPanel;
import lakbaylokal.barangay.reports.BarangayReportsPanel;
import lakbaylokal.barangay.verification.BarangayVerificationPanel;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;

import javax.swing.*;
import java.awt.*;

public class BarangayDashboard extends JFrame {
    private final BarangayController controller;
    private JTabbedPane tabbedPane;

    private BarangayVerificationPanel verificationPanel;
    private BarangayDestinationsPanel destinationsPanel;
    private BarangayReportsPanel reportsPanel;

    public BarangayDashboard() {
        this.controller = new BarangayController();
        controller.setView(this);

        setTitle("Lakbay Lokal — Barangay Tourism Desk (Biliran Province)");
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
        String name = (currentUser != null) ? currentUser.getFullName() : "Captain Ramon Corpin (Barangay Official)";
        root.add(UITheme.createHeaderBanner("Barangay Tourism Desk", name, controller::handleLogout), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(UITheme.TEXT_DARK);

        verificationPanel = new BarangayVerificationPanel();
        destinationsPanel = new BarangayDestinationsPanel();
        reportsPanel = new BarangayReportsPanel();

        tabbedPane.addTab("🛡️ Stakeholder Clearances & Accreditation", verificationPanel);
        tabbedPane.addTab("📍 Local Attractions & Safety Advisories", destinationsPanel);
        tabbedPane.addTab("📊 Barangay Tourism & Livelihood Reports", reportsPanel);

        // Auto-refresh when tab is clicked
        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            switch (selected) {
                case 0 -> verificationPanel.loadVerifications();
                case 1 -> destinationsPanel.loadDestinations();
                case 2 -> reportsPanel.loadStats();
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    public static void main(String[] args) {
        UITheme.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            BarangayDashboard frame = new BarangayDashboard();
            frame.setVisible(true);
        });
    }
}
