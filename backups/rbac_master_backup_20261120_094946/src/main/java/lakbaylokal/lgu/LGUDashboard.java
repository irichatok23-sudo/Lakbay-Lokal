package lakbaylokal.lgu;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.lgu.monitoring.LGUMonitoringPanel;
import lakbaylokal.lgu.reports.LGUReportsPanel;
import lakbaylokal.lgu.statistics.LGUStatisticsPanel;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;

import javax.swing.*;
import java.awt.*;

public class LGUDashboard extends JFrame {
    private final LGUController controller;
    private JTabbedPane tabbedPane;

    private LGUStatisticsPanel statisticsPanel;
    private LGUMonitoringPanel monitoringPanel;
    private LGUReportsPanel reportsPanel;

    public LGUDashboard() {
        this.controller = new LGUController();
        controller.setView(this);

        setTitle("Lakbay Lokal — Local Government Unit (LGU) Portal • Biliran Province");
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

        statisticsPanel = new LGUStatisticsPanel();
        monitoringPanel = new LGUMonitoringPanel();
        reportsPanel = new LGUReportsPanel();

        tabbedPane.addTab("  📈 Provincial Statistics  ", statisticsPanel);
        tabbedPane.addTab("  🌿 Environmental Monitoring  ", monitoringPanel);
        tabbedPane.addTab("  📑 Governance & Audit Reports  ", reportsPanel);

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            if (selected == 0 && statisticsPanel != null) {
                statisticsPanel.loadStatistics();
            } else if (selected == 1 && monitoringPanel != null) {
                monitoringPanel.loadCarryingCapacityData();
            } else if (selected == 2 && reportsPanel != null) {
                reportsPanel.generateReportContent();
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(UITheme.COLOR_PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel brand = new JLabel("LAKBAY LOKAL");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JLabel tag = new JLabel("Local Government Unit (LGU) Tourism Desk • Municipal Operations & Governance");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(new Color(209, 250, 229));

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.add(brand);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(tag);

        User currentUser = SessionManager.getCurrentUser();
        String name = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Mayor Antonio Velasquez";

        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userBox.setOpaque(false);

        JLabel userLabel = new JLabel("🏛️ " + name + " (LGU Official)");
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(Color.WHITE);

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of the LGU Portal?",
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
            LGUDashboard frame = new LGUDashboard();
            frame.setVisible(true);
        });
    }
}
