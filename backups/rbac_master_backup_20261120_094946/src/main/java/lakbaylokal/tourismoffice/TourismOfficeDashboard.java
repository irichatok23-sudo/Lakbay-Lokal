package lakbaylokal.tourismoffice;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;
import lakbaylokal.tourismoffice.monitoring.TourismMonitoringPanel;
import lakbaylokal.tourismoffice.promotions.TourismPromotionsPanel;
import lakbaylokal.tourismoffice.reports.TourismReportsPanel;

import javax.swing.*;
import java.awt.*;

public class TourismOfficeDashboard extends JFrame {
    private final TourismOfficeController controller;
    private JTabbedPane tabbedPane;

    private TourismPromotionsPanel promotionsPanel;
    private TourismMonitoringPanel monitoringPanel;
    private TourismReportsPanel reportsPanel;

    public TourismOfficeDashboard() {
        this.controller = new TourismOfficeController();
        controller.setView(this);

        setTitle("Lakbay Lokal — Provincial Tourism Office Portal • Biliran Province");
        setSize(1220, 820);
        setMinimumSize(new Dimension(1020, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.COLOR_BG);

        root.add(createHeader(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SUBTITLE);
        tabbedPane.setBackground(UITheme.COLOR_CARD_BG);
        tabbedPane.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        promotionsPanel = new TourismPromotionsPanel();
        monitoringPanel = new TourismMonitoringPanel();
        reportsPanel = new TourismReportsPanel();

        tabbedPane.addTab("  📢 Campaigns & Promotions  ", promotionsPanel);
        tabbedPane.addTab("  🔎 Destination & DOT Standards  ", monitoringPanel);
        tabbedPane.addTab("  📊 DOT Regional Reports  ", reportsPanel);

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            if (selected == 0 && promotionsPanel != null) {
                promotionsPanel.loadAnnouncements();
            } else if (selected == 1 && monitoringPanel != null) {
                monitoringPanel.loadMonitoringData();
            } else if (selected == 2 && reportsPanel != null) {
                reportsPanel.generateReportContent();
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(UITheme.COLOR_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel brand = new JLabel("LAKBAY LOKAL");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JLabel tag = new JLabel("Provincial Tourism Office • Biliran Province, Region VIII (Eastern Visayas)");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(new Color(209, 250, 229));

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.add(brand);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(tag);

        User currentUser = SessionManager.getCurrentUser();
        String name = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Elena Cruz (Tourism Officer)";

        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userBox.setOpaque(false);

        JLabel userLabel = new JLabel("🌴 " + name + " (Tourism Officer)");
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(Color.WHITE);

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of the Provincial Tourism Office Portal?",
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TourismOfficeDashboard frame = new TourismOfficeDashboard();
            frame.setVisible(true);
        });
    }
}
