package lakbaylokal.authentication;

import lakbaylokal.shared.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegistrationFrame extends JFrame {
    private final AuthenticationService authService = new AuthenticationService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField originCityField;

    public RegistrationFrame() {
        setTitle("Lakbay Lokal — Tourist Account Registration");
        setSize(540, 680);
        setMinimumSize(new Dimension(480, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Create Tourist Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Join Lakbay Lokal to explore Biliran Island destinations & support local communities.");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(new Color(204, 251, 241));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // Form in Card
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridLayout(8, 2, 12, 14));
        formCard.setBorder(new EmptyBorder(20, 24, 20, 24));

        usernameField = createStyledTextField();
        passwordField = createStyledPasswordField();
        confirmPasswordField = createStyledPasswordField();
        fullNameField = createStyledTextField();
        emailField = createStyledTextField();
        phoneField = createStyledTextField();
        phoneField.setText("+63 9");
        originCityField = createStyledTextField();
        originCityField.setText("Tacloban City");

        formCard.add(new JLabel("Desired Username:"));
        formCard.add(usernameField);
        formCard.add(new JLabel("Password (min. 6 chars):"));
        formCard.add(passwordField);
        formCard.add(new JLabel("Confirm Password:"));
        formCard.add(confirmPasswordField);
        formCard.add(new JLabel("Full Name:"));
        formCard.add(fullNameField);
        formCard.add(new JLabel("Email Address:"));
        formCard.add(emailField);
        formCard.add(new JLabel("Contact Mobile No:"));
        formCard.add(phoneField);
        formCard.add(new JLabel("Origin City / Province:"));
        formCard.add(originCityField);

        JButton cancelBtn = UITheme.createSecondaryButton("← Back to Sign In");
        cancelBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JButton registerBtn = UITheme.createPrimaryButton("Register Account");
        registerBtn.addActionListener(e -> handleRegistration());

        formCard.add(cancelBtn);
        formCard.add(registerBtn);

        cardWrapper.add(formCard);
        root.add(cardWrapper, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    private void handleRegistration() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();
        String cp = new String(confirmPasswordField.getPassword()).trim();
        String fn = fullNameField.getText().trim();
        String em = emailField.getText().trim();
        String ph = phoneField.getText().trim();
        String city = originCityField.getText().trim();

        if (u.isEmpty() || p.isEmpty() || fn.isEmpty() || em.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory account fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!p.equals(cp)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (p.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!em.contains("@") || !em.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = authService.registerTourist(u, p, em, fn, ph, city);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Welcome to Lakbay Lokal! Registration complete. You can now sign in.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            // Provide informative message
            JOptionPane.showMessageDialog(this,
                    "Unable to create account. Either the username or email is already in use,\nor database connection is offline.",
                    "Registration Notice", JOptionPane.WARNING_MESSAGE);
        }
    }
}
