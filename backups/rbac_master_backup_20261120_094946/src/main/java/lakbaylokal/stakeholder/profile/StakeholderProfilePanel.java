package lakbaylokal.stakeholder.profile;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.StakeholderProfile;
import lakbaylokal.model.User;
import lakbaylokal.service.StakeholderService;
import lakbaylokal.shared.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StakeholderProfilePanel extends JPanel {
    private final StakeholderService stakeholderService = new StakeholderService();

    private JTextField businessNameField;
    private JComboBox<String> businessTypeCombo;
    private JTextField accreditationNoField;
    private JTextField contactField;
    private JTextField experienceYearsField;
    private JTextField municipalityField;
    private JTextField barangayField;
    private JTextField addressField;
    private JLabel statusBadge;
    private JLabel accreditationIdLabel;
    private JLabel verifiedInfoLabel;

    private StakeholderProfile currentProfile;

    public StakeholderProfilePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);

        loadProfileData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Stakeholder Enterprise Profile & Clearances");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Manage your community enterprise information, local services, and official accreditation status.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Profile");
        refreshBtn.addActionListener(e -> loadProfileData());

        header.add(textPanel, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent createFormPanel() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setOpaque(false);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Status Badge Banner
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(new JLabel("Official Accreditation Status:"));

        statusBadge = new JLabel(" VERIFIED ");
        statusBadge.setFont(UITheme.FONT_BOLD);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(UITheme.SUCCESS_LIGHT);
        statusBadge.setForeground(new Color(5, 150, 105));
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.SUCCESS, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        badgePanel.add(statusBadge);

        accreditationIdLabel = new JLabel("Reg ID: BLR-STK-003");
        accreditationIdLabel.setFont(UITheme.FONT_SMALL);
        accreditationIdLabel.setForeground(UITheme.TEXT_MUTED);
        badgePanel.add(accreditationIdLabel);

        verifiedInfoLabel = new JLabel("• Endorsed by Barangay Ol-og & PCG");
        verifiedInfoLabel.setFont(UITheme.FONT_SMALL);
        verifiedInfoLabel.setForeground(UITheme.TEAL);
        badgePanel.add(verifiedInfoLabel);

        card.add(badgePanel, gbc);

        // Row 1: Business Name
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        card.add(new JLabel("Business / Enterprise Name:"), gbc);
        gbc.gridx = 1;
        businessNameField = createTextField(28);
        card.add(businessNameField, gbc);

        // Row 2: Business Type
        gbc.gridy = 2;
        gbc.gridx = 0;
        card.add(new JLabel("Enterprise Category:"), gbc);
        gbc.gridx = 1;
        String[] types = {"BOAT_OPERATOR", "TOUR_GUIDE", "TRANSPORT_DRIVER", "LOCAL_COOK", "ARTISAN"};
        businessTypeCombo = new JComboBox<>(types);
        businessTypeCombo.setFont(UITheme.FONT_BODY);
        card.add(businessTypeCombo, gbc);

        // Row 3: Accreditation Number
        gbc.gridy = 3;
        gbc.gridx = 0;
        card.add(new JLabel("Accreditation / Permit No:"), gbc);
        gbc.gridx = 1;
        accreditationNoField = createTextField(28);
        card.add(accreditationNoField, gbc);

        // Row 4: Contact Number
        gbc.gridy = 4;
        gbc.gridx = 0;
        card.add(new JLabel("Contact Mobile No:"), gbc);
        gbc.gridx = 1;
        contactField = createTextField(28);
        card.add(contactField, gbc);

        // Row 5: Experience
        gbc.gridy = 5;
        gbc.gridx = 0;
        card.add(new JLabel("Years of Tourism Experience:"), gbc);
        gbc.gridx = 1;
        experienceYearsField = createTextField(28);
        card.add(experienceYearsField, gbc);

        // Row 6: Municipality & Barangay
        gbc.gridy = 6;
        gbc.gridx = 0;
        card.add(new JLabel("Municipality & Barangay:"), gbc);
        gbc.gridx = 1;
        JPanel locGrid = new JPanel(new GridLayout(1, 2, 8, 0));
        locGrid.setOpaque(false);
        municipalityField = createTextField(14);
        barangayField = createTextField(14);
        locGrid.add(municipalityField);
        locGrid.add(barangayField);
        card.add(locGrid, gbc);

        // Row 7: Physical Address
        gbc.gridy = 7;
        gbc.gridx = 0;
        card.add(new JLabel("Base / Docking Address:"), gbc);
        gbc.gridx = 1;
        addressField = createTextField(28);
        card.add(addressField, gbc);

        // Row 8: Save Button
        gbc.gridy = 8;
        gbc.gridx = 1;
        JButton saveBtn = UITheme.createPrimaryButton("Save Profile Changes");
        saveBtn.addActionListener(e -> saveProfile());
        card.add(saveBtn, gbc);

        container.add(card, BorderLayout.CENTER);
        return container;
    }

    private JTextField createTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public void loadProfileData() {
        User user = SessionManager.getCurrentUser();
        int userId = (user != null) ? user.getUserId() : 7; // Default Capitan Danilo Flores

        currentProfile = stakeholderService.getStakeholderByUserId(userId);
        if (currentProfile == null) {
            // Check by ID 3 (Maripipi Boats) or 1 (Guides)
            currentProfile = stakeholderService.getProfileById(3);
        }
        if (currentProfile == null) {
            currentProfile = stakeholderService.getProfileById(1);
        }

        if (currentProfile != null) {
            businessNameField.setText(currentProfile.getBusinessName());
            if (currentProfile.getCategory() != null) {
                businessTypeCombo.setSelectedItem(currentProfile.getCategory());
            } else if (currentProfile.getBusinessType() != null) {
                businessTypeCombo.setSelectedItem(currentProfile.getBusinessType());
            }
            accreditationNoField.setText(currentProfile.getAccreditationNo() != null ? currentProfile.getAccreditationNo() : "MARINA-EV-2024-099");
            contactField.setText(currentProfile.getContactNumber() != null ? currentProfile.getContactNumber() : "+63 928 333 7777");
            experienceYearsField.setText(String.valueOf(currentProfile.getYearsExperience() > 0 ? currentProfile.getYearsExperience() : 10));
            municipalityField.setText(currentProfile.getMunicipality() != null ? currentProfile.getMunicipality() : "Maripipi");
            barangayField.setText(currentProfile.getBarangay() != null ? currentProfile.getBarangay() : "Ol-og (Sambawan)");
            addressField.setText(currentProfile.getAddress() != null ? currentProfile.getAddress() : "Kawayan Port / Maripipi Pier");
            accreditationIdLabel.setText("Reg ID: BLR-STK-00" + currentProfile.getProfileId());

            String status = currentProfile.getVerificationStatus() != null ? currentProfile.getVerificationStatus() : currentProfile.getAccreditationStatus();
            if (status != null && status.equalsIgnoreCase("VERIFIED")) {
                statusBadge.setText(" VERIFIED & ENDORSED ");
                statusBadge.setBackground(UITheme.SUCCESS_LIGHT);
                statusBadge.setForeground(new Color(5, 150, 105));
            } else if (status != null && status.equalsIgnoreCase("REJECTED")) {
                statusBadge.setText(" REJECTED ");
                statusBadge.setBackground(UITheme.DANGER_LIGHT);
                statusBadge.setForeground(new Color(185, 28, 28));
            } else {
                statusBadge.setText(" PENDING ACCREDITATION ");
                statusBadge.setBackground(UITheme.ACCENT_AMBER_LIGHT);
                statusBadge.setForeground(new Color(180, 83, 9));
            }
        }
    }

    private void saveProfile() {
        if (businessNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Business enterprise name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentProfile == null) {
            currentProfile = new StakeholderProfile();
            User user = SessionManager.getCurrentUser();
            currentProfile.setUserId((user != null) ? user.getUserId() : 7);
        }

        currentProfile.setBusinessName(businessNameField.getText().trim());
        String cat = (String) businessTypeCombo.getSelectedItem();
        currentProfile.setCategory(cat);
        currentProfile.setBusinessType(cat);
        currentProfile.setAccreditationNo(accreditationNoField.getText().trim());
        currentProfile.setContactNumber(contactField.getText().trim());
        try {
            currentProfile.setYearsExperience(Integer.parseInt(experienceYearsField.getText().trim()));
        } catch (Exception ignored) {
            currentProfile.setYearsExperience(5);
        }
        currentProfile.setMunicipality(municipalityField.getText().trim());
        currentProfile.setBarangay(barangayField.getText().trim());
        currentProfile.setAddress(addressField.getText().trim());

        boolean ok = stakeholderService.updateStakeholderProfile(currentProfile);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Enterprise profile updated and saved to database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProfileData();
        } else {
            JOptionPane.showMessageDialog(this, "Profile updated locally. Ensure database is running for full persistence.", "Notice", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
