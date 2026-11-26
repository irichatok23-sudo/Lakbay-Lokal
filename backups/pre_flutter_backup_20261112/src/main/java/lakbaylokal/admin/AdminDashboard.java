package lakbaylokal.admin;

import lakbaylokal.admin.backup.AdminBackupPanel;
import lakbaylokal.admin.bookings.AdminBookingsPanel;
import lakbaylokal.admin.destinations.AdminDestinationsPanel;
import lakbaylokal.admin.packages.AdminPackagesPanel;
import lakbaylokal.admin.reports.AdminReportsPanel;
import lakbaylokal.admin.roles.AdminRolesPanel;
import lakbaylokal.admin.security.AdminSecurityPanel;
import lakbaylokal.admin.users.AdminUsersPanel;
import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private final AdminController controller;
    private JTabbedPane tabbedPane;

    private AdminUsersPanel usersPanel;
    private AdminRolesPanel rolesPanel;
    private AdminDestinationsPanel destinationsPanel;
    private AdminPackagesPanel packagesPanel;
    private AdminBookingsPanel bookingsPanel;
    private AdminReportsPanel reportsPanel;
    private AdminSecurityPanel securityPanel;
    private AdminBackupPanel backupPanel;

    public AdminDashboard() {
        this.controller = new AdminController();
        controller.setView(this);

        setTitle("Lakbay Lokal — Master System Administration Dashboard");
        setSize(1280, 850);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.COLOR_BG);

        // Header Panel (Brand + User info)
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(createHeader(), BorderLayout.NORTH);
        topContainer.add(createRoleLauncherBar(), BorderLayout.SOUTH);

        root.add(topContainer, BorderLayout.NORTH);

        // Tabbed Pane for all 8 admin features
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SUBTITLE);
        tabbedPane.setBackground(UITheme.COLOR_CARD_BG);
        tabbedPane.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        usersPanel = new AdminUsersPanel();
        rolesPanel = new AdminRolesPanel();
        destinationsPanel = new AdminDestinationsPanel();
        packagesPanel = new AdminPackagesPanel();
        bookingsPanel = new AdminBookingsPanel();
        reportsPanel = new AdminReportsPanel();
        securityPanel = new AdminSecurityPanel();
        backupPanel = new AdminBackupPanel();

        tabbedPane.addTab("  👥 Users  ", usersPanel);
        tabbedPane.addTab("  🛡️ Roles & Matrix  ", rolesPanel);
        tabbedPane.addTab("  📍 Destinations  ", destinationsPanel);
        tabbedPane.addTab("  🎒 Packages  ", packagesPanel);
        tabbedPane.addTab("  📋 Bookings  ", bookingsPanel);
        tabbedPane.addTab("  📊 Reports & GMV  ", reportsPanel);
        tabbedPane.addTab("  🔒 Security & Audit  ", securityPanel);
        tabbedPane.addTab("  💾 Backup & Restore  ", backupPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            switch (idx) {
                case 0 -> { if (usersPanel != null) usersPanel.loadUsers(); }
                case 2 -> { if (destinationsPanel != null) destinationsPanel.loadDestinations(); }
                case 3 -> { if (packagesPanel != null) packagesPanel.loadPackages(); }
                case 4 -> { if (bookingsPanel != null) bookingsPanel.loadBookings(); }
                case 5 -> { if (reportsPanel != null) reportsPanel.loadStats(); }
                case 6 -> { if (securityPanel != null) securityPanel.loadLogs(); }
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Slate 900
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel brand = new JLabel("LAKBAY LOKAL — MASTER ADMINISTRATION");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JLabel tag = new JLabel("Central Management Console • Full System Authority across all 6 roles");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(new Color(148, 163, 184));

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.add(brand);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(tag);

        User currentUser = SessionManager.getCurrentUser();
        String name = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Administrator";

        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userBox.setOpaque(false);

        JLabel userLabel = new JLabel("⚡ " + name + " (System Admin)");
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(new Color(248, 250, 252));

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of Lakbay Lokal?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                controller.handleLogout();
            }
        });

        userBox.add(userLabel);
        userBox.add(logoutBtn);

        header.add(brandBox, BorderLayout.WEST);
        header.add(userBox, BorderLayout.EAST);
        return header;
    }

    private JPanel createRoleLauncherBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(30, 41, 59)); // Slate 800
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(51, 65, 85)));

        JLabel label = new JLabel("Quick Launch Sub-Dashboards:");
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(new Color(226, 232, 240));
        bar.add(label);

        JButton touristBtn = createLauncherButton("🌴 Tourist Portal", UITheme.COLOR_PRIMARY);
        touristBtn.addActionListener(e -> controller.launchTouristView());

        JButton stakeholderBtn = createLauncherButton("🚤 Stakeholder Portal", UITheme.COLOR_ACCENT_GREEN);
        stakeholderBtn.addActionListener(e -> controller.launchStakeholderView());

        JButton barangayBtn = createLauncherButton("🏛️ Barangay Desk", new Color(4, 120, 87));
        barangayBtn.addActionListener(e -> controller.launchBarangayView());

        JButton lguBtn = createLauncherButton("🏢 LGU Municipal Desk", new Color(30, 64, 175));
        lguBtn.addActionListener(e -> controller.launchLGUView());

        JButton tourismOfficeBtn = createLauncherButton("📢 Tourism Office", new Color(15, 118, 110));
        tourismOfficeBtn.addActionListener(e -> controller.launchTourismOfficeView());

        bar.add(touristBtn);
        bar.add(stakeholderBtn);
        bar.add(barangayBtn);
        bar.add(lguBtn);
        bar.add(tourismOfficeBtn);

        return bar;
    }

    private JButton createLauncherButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_SMALL);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard frame = new AdminDashboard();
            frame.setVisible(true);
        });
    }
}
