package lakbaylokal.admin.roles;

import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminRolesPanel extends JPanel {
    private JTable rolesTable;

    public AdminRolesPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createRolesMatrix(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("System Roles & Access Control Matrix (RBAC)");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Role-based access boundaries and security permissions across the six system modules.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.COLOR_TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton exportBtn = UITheme.createSecondaryButton("Export Matrix CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(rolesTable, "Admin_Roles_Access_Control_Matrix"));
        actionPanel.add(exportBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);
        return header;
    }

    private JComponent createRolesMatrix() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setOpaque(false);

        String[] cols = {"Role ID", "Role Code", "Role Title", "Primary Scope", "Access Permissions & Boundaries", "Target Office / User Group"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        model.addRow(new Object[]{1, "SYSTEM_ADMIN", "System Administrator", "Full System", "Master user management, role assignments, master catalogs, audit trails, database backup & recovery", "IT Officers & Master Administrators"});
        model.addRow(new Object[]{2, "TOURISM_OFFICE", "Provincial Tourism Officer", "Province-Wide", "Publish province campaigns & advisories, DOT quality monitoring, regional statistical submissions", "Provincial Tourism Office (Biliran)"});
        model.addRow(new Object[]{3, "LGU_OFFICER", "Local Government Unit Officer", "Municipal Government", "Carrying capacity monitoring, maritime advisories, municipal statistics, governance audit reports", "Municipal Mayors, Administrators & Planners"});
        model.addRow(new Object[]{4, "BARANGAY_OFFICIAL", "Barangay Official", "Barangay Jurisdiction", "Accreditation endorsements, operational site advisories, tourist arrival logging, barangay reports", "Barangay Tourism Desk & Council"});
        model.addRow(new Object[]{5, "STAKEHOLDER", "Local Tourism Stakeholder", "Community Business", "Enterprise profile management, service catalog CRUD, rate & schedule updates, booking fulfillment", "Motorized Banca Operators, Tour Guides, Lodges"});
        model.addRow(new Object[]{6, "TOURIST", "Visiting Tourist", "Public Traveler", "Browse attractions, customize tour packages, make bookings, view itinerary vouchers, write reviews", "Visiting Travelers & Eco-Tourists"});

        rolesTable = new JTable(model);
        UITheme.styleTable(rolesTable);

        JScrollPane scrollPane = new JScrollPane(rolesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // Note Card
        JPanel infoCard = UITheme.createCardPanel();
        infoCard.setLayout(new BorderLayout());
        JLabel note = new JLabel("<html><b>Security Policy Enforcement:</b> Role-based access control (RBAC) is enforced cryptographically upon authentication and verified at the controller layer. User sessions cannot access actions reserved for other role tiers.</html>");
        note.setFont(UITheme.FONT_BODY);
        infoCard.add(note, BorderLayout.CENTER);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(infoCard, BorderLayout.SOUTH);
        return container;
    }
}
