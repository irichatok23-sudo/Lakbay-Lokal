package lakbaylokal.authentication;

import lakbaylokal.admin.AdminDashboard;
import lakbaylokal.barangay.BarangayDashboard;
import lakbaylokal.database.ConnectionFactory;
import lakbaylokal.lgu.LGUDashboard;
import lakbaylokal.model.User;
import lakbaylokal.shared.UITheme;
import lakbaylokal.stakeholder.StakeholderDashboard;
import lakbaylokal.tourist.TouristDashboard;
import lakbaylokal.tourismoffice.TourismOfficeDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthenticationService authService = new AuthenticationService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox chkShowPassword;
    private JLabel dbStatusLabel;

    public LoginFrame() {
        setTitle("Lakbay Lokal — Community-Based Tourism Information System (Biliran)");
        setSize(960, 680);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Split Layout: Left Hero Panel (40%), Right Auth & Launch Panel (60%)
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Hero Banner
        gbc.gridx = 0;
        gbc.weightx = 0.42;
        mainContainer.add(createHeroPanel(), gbc);

        // Right Content Area (Tabs: Login Form & Role Launcher)
        gbc.gridx = 1;
        gbc.weightx = 0.58;
        mainContainer.add(createAuthCard(), gbc);

        root.add(mainContainer, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.PRIMARY_DARK,
                        getWidth(), getHeight(), UITheme.PRIMARY
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setBorder(new EmptyBorder(40, 36, 36, 36));

        // Top Brand Information
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);

        JLabel logoIcon = new JLabel("🌴");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));

        JLabel brandTitle = new JLabel("LAKBAY LOKAL");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("Community-Based Tourism Information System");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brandSubtitle.setForeground(UITheme.TEAL_LIGHT);

        JLabel regionTag = new JLabel("Province of Biliran • Region VIII, Philippines");
        regionTag.setFont(UITheme.FONT_SMALL);
        regionTag.setForeground(new Color(167, 243, 208));

        topBox.add(logoIcon);
        topBox.add(Box.createVerticalStrut(10));
        topBox.add(brandTitle);
        topBox.add(Box.createVerticalStrut(4));
        topBox.add(brandSubtitle);
        topBox.add(Box.createVerticalStrut(2));
        topBox.add(regionTag);
        topBox.add(Box.createVerticalStrut(30));

        // Center Value Proposition Highlights
        JPanel featureList = new JPanel();
        featureList.setLayout(new BoxLayout(featureList, BoxLayout.Y_AXIS));
        featureList.setOpaque(false);

        featureList.add(createFeatureItem("✓", "Direct Community Booking & Guides", "Connect tourists directly with verified local boatmen, homestays, and tour guides."));
        featureList.add(Box.createVerticalStrut(18));
        featureList.add(createFeatureItem("✓", "Barangay Clearances & Oversight", "Barangay tourism council verification and safety compliance for local operators."));
        featureList.add(Box.createVerticalStrut(18));
        featureList.add(createFeatureItem("✓", "Carrying Capacity & Eco-Tracking", "Real-time ecological monitoring safeguarding Sambawan, Tinago, and pristine spots."));
        featureList.add(Box.createVerticalStrut(18));
        featureList.add(createFeatureItem("✓", "Standardized DOT Regional Reports", "Empowering 8 Municipal LGUs and Provincial Tourism Office with live analytics."));

        // Bottom Status Box
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);

        boolean dbOk = ConnectionFactory.testConnection();
        String dbText = dbOk ? "● Database: Connected (MySQL 3306)" : "○ Standby: Local Demo & Fallback Ready";
        dbStatusLabel = new JLabel(dbText);
        dbStatusLabel.setFont(UITheme.FONT_SMALL);
        dbStatusLabel.setForeground(dbOk ? new Color(110, 231, 183) : new Color(253, 230, 138));

        bottomBox.add(dbStatusLabel, BorderLayout.SOUTH);

        hero.add(topBox, BorderLayout.NORTH);
        hero.add(featureList, BorderLayout.CENTER);
        hero.add(bottomBox, BorderLayout.SOUTH);

        return hero;
    }

    private JPanel createFeatureItem(String icon, String title, String desc) {
        JPanel item = new JPanel(new BorderLayout(10, 2));
        item.setOpaque(false);

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI", Font.BOLD, 15));
        ic.setForeground(UITheme.SUCCESS_LIGHT);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.WHITE);

        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(UITheme.FONT_SMALL);
        d.setForeground(new Color(209, 250, 229));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(t);
        text.add(Box.createVerticalStrut(2));
        text.add(d);

        item.add(ic, BorderLayout.WEST);
        item.add(text, BorderLayout.CENTER);
        return item;
    }

    private JPanel createAuthCard() {
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(UITheme.BG_LIGHT);
        rightWrapper.setBorder(new EmptyBorder(30, 36, 30, 36));

        rightWrapper.add(createLoginForm(), BorderLayout.CENTER);
        return rightWrapper;
    }

    private JPanel createLoginForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Sign In to Your Account");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("Enter your registered credentials to access your dashboard");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(20));

        // Username
        form.add(new JLabel("Username or Email:"));
        form.add(Box.createVerticalStrut(6));
        usernameField = new JTextField();
        usernameField.setFont(UITheme.FONT_BODY);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));

        // Password
        form.add(new JLabel("Password:"));
        form.add(Box.createVerticalStrut(6));
        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(6));

        // Show password checkbox
        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(UITheme.FONT_SMALL);
        chkShowPassword.setBackground(Color.WHITE);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        form.add(chkShowPassword);
        form.add(Box.createVerticalStrut(18));

        // Sign In Button
        JButton loginBtn = UITheme.createPrimaryButton("Sign In to Portal");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> handleLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(10));

        // Registration Button
        JButton registerBtn = UITheme.createSecondaryButton("Register as New Tourist");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerBtn.addActionListener(e -> {
            dispose();
            new RegistrationFrame().setVisible(true);
        });
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(20));

        // Quick test account switcher dropdown
        JPanel quickFillBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickFillBox.setBackground(Color.WHITE);
        quickFillBox.add(new JLabel("Quick Fill Account:"));

        String[] demoAccounts = {
                "tourist_ana (Tourist)",
                "boat_capitan (Stakeholder)",
                "brgy_olog (Barangay)",
                "lgu_naval (LGU)",
                "tourismoffice (Tourism Office)",
                "admin (System Admin)"
        };
        JComboBox<String> cmbFill = new JComboBox<>(demoAccounts);
        cmbFill.setFont(UITheme.FONT_SMALL);
        cmbFill.addActionListener(e -> {
            String selected = (String) cmbFill.getSelectedItem();
            if (selected != null) {
                String u = selected.split(" ")[0];
                usernameField.setText(u);
                passwordField.setText("password123");
            }
        });
        quickFillBox.add(cmbFill);
        form.add(quickFillBox);

        return form;
    }

    @SuppressWarnings("unused")
    private JPanel createQuickDemoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("⚡ Instant Multi-Role Portal Switcher");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel desc = new JLabel("Click any button below to instantly launch that user role's full dashboard:");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_MUTED);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(desc);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createRoleButton("1. Tourist (Ana Sophia Cortez)", "Explore destinations, custom itinerary, reserve packages, publish reviews", 6, new Color(16, 185, 129)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createRoleButton("2. Local Stakeholder (Capitan Danilo Flores)", "Maripipi Boat Operator • Manage services, departure schedules, bookings", 5, new Color(13, 148, 136)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createRoleButton("3. Barangay Official (Captain Ramon Corpin)", "Ol-og / Sambawan Desk • Verify clearances, local spots, livelihood reports", 4, new Color(22, 101, 52)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createRoleButton("4. LGU Officer (Officer Katrina Gonzales)", "Naval Tourism Office • Carrying capacity alerts, statistics & municipal audits", 3, new Color(30, 64, 175)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createRoleButton("5. Tourism Office (Director Eduardo Reyes)", "Provincial Desk • Campaigns, promotional advisories & DOT Form 2A reports", 2, new Color(15, 118, 110)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createRoleButton("6. System Administrator (Maria Santos)", "Master Admin Console • User RBAC, destinations/packages CRUD, audit logs, backup", 1, new Color(15, 23, 42)));

        return panel;
    }

    private JButton createRoleButton(String title, String roleDesc, int roleId, Color color) {
        JButton btn = new JButton("<html><b>" + title + "</b><br><span style='font-size:10px; color:#f1f5f9;'>" + roleDesc + "</span></html>");
        btn.setFont(UITheme.FONT_BODY);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> launchRoleDirectly(roleId));
        return btn;
    }

    private void handleLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!lakbaylokal.database.ConnectionFactory.testConnection()) {
            JOptionPane.showMessageDialog(this,
                    "Database server connection error. Unable to reach MySQL on localhost:3306.\nPlease start MySQL via WAMP or XAMPP and try again.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = authService.authenticate(u, p);
        if (user != null) {
            routeUserToDashboard(user);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Invalid credentials or inactive user account. Please check your username and password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    public void launchRoleDirectly(int roleId) {
        User simulatedUser = new User();
        simulatedUser.setRoleId(roleId);
        simulatedUser.setActive(true);

        switch (roleId) {
            case 1 -> {
                simulatedUser.setUserId(1);
                simulatedUser.setUsername("admin");
                simulatedUser.setFullName("Administrator Maria Santos");
                simulatedUser.setEmail("admin@lakbaylokal.gov.ph");
                simulatedUser.setRoleName("System Administrator");
                simulatedUser.setRoleCode("SYSTEM_ADMIN");
                SessionManager.setCurrentUser(simulatedUser);
                dispose();
                new AdminDashboard().setVisible(true);
            }
            case 2 -> {
                simulatedUser.setUserId(2);
                simulatedUser.setUsername("tourismoffice");
                simulatedUser.setFullName("Director Eduardo Reyes");
                simulatedUser.setEmail("tourism@biliran.gov.ph");
                simulatedUser.setRoleName("Tourism Office Officer");
                simulatedUser.setRoleCode("TOURISM_OFFICE");
                SessionManager.setCurrentUser(simulatedUser);
                dispose();
                new TourismOfficeDashboard().setVisible(true);
            }
            case 3 -> {
                simulatedUser.setUserId(3);
                simulatedUser.setUsername("lgu_naval");
                simulatedUser.setFullName("Officer Katrina Gonzales");
                simulatedUser.setEmail("lgu.naval@biliran.gov.ph");
                simulatedUser.setRoleName("Local Government Unit Officer");
                simulatedUser.setRoleCode("LGU_OFFICER");
                SessionManager.setCurrentUser(simulatedUser);
                dispose();
                new LGUDashboard().setVisible(true);
            }
            case 4 -> {
                simulatedUser.setUserId(4);
                simulatedUser.setUsername("brgy_olog");
                simulatedUser.setFullName("Captain Ramon Corpin");
                simulatedUser.setEmail("brgy.olog@maripipi.gov.ph");
                simulatedUser.setRoleName("Barangay Official");
                simulatedUser.setRoleCode("BARANGAY_OFFICIAL");
                SessionManager.setCurrentUser(simulatedUser);
                dispose();
                new BarangayDashboard().setVisible(true);
            }
            case 5 -> {
                simulatedUser.setUserId(7); // Seed user 7 is Capitan Danilo Flores
                simulatedUser.setUsername("boat_capitan");
                simulatedUser.setFullName("Capitan Danilo Flores");
                simulatedUser.setEmail("maripipi.boats@gmail.com");
                simulatedUser.setRoleName("Local Tourism Stakeholder");
                simulatedUser.setRoleCode("STAKEHOLDER");
                SessionManager.setCurrentUser(simulatedUser);
                dispose();
                new StakeholderDashboard().setVisible(true);
            }
            case 6 -> {
                simulatedUser.setUserId(10); // Seed user 10 is Ana Sophia Cortez
                simulatedUser.setUsername("tourist_ana");
                simulatedUser.setFullName("Ana Sophia Cortez");
                simulatedUser.setEmail("ana.tourist@gmail.com");
                simulatedUser.setRoleName("Tourist");
                simulatedUser.setRoleCode("TOURIST");
                SessionManager.setCurrentUser(simulatedUser);
                dispose();
                new TouristDashboard().setVisible(true);
            }
        }
    }

    private void routeUserToDashboard(User user) {
        SessionManager.setCurrentUser(user);
        dispose();
        int roleId = user.getRoleId();
        switch (roleId) {
            case 1 -> new AdminDashboard().setVisible(true);
            case 2 -> new TourismOfficeDashboard().setVisible(true);
            case 3 -> new LGUDashboard().setVisible(true);
            case 4 -> new BarangayDashboard().setVisible(true);
            case 5 -> new StakeholderDashboard().setVisible(true);
            case 6 -> new TouristDashboard().setVisible(true);
            default -> new TouristDashboard().setVisible(true);
        }
    }

    public static void main(String[] args) {
        UITheme.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
