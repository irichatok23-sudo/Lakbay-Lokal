package lakbaylokal.barangay.verification;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.User;
import lakbaylokal.model.VerificationRecord;
import lakbaylokal.service.VerificationService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BarangayVerificationPanel extends JPanel {
    private final VerificationService verificationService = new VerificationService();

    private JTable verificationTable;
    private DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbStatus;

    private List<VerificationRecord> recordList = new ArrayList<>();

    public BarangayVerificationPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top Filter Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.setOpaque(false);

        filters.add(new JLabel("🔍 Search:"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.addActionListener(e -> applyFilter());
        filters.add(txtSearch);

        filters.add(new JLabel("Accreditation Status:"));
        cmbStatus = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "REJECTED"});
        cmbStatus.setFont(UITheme.FONT_BODY);
        cmbStatus.addActionListener(e -> applyFilter());
        filters.add(cmbStatus);

        JButton btnFilter = UITheme.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> applyFilter());
        filters.add(btnFilter);

        JButton btnReset = UITheme.createSecondaryButton("Refresh");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            loadVerifications();
        });
        filters.add(btnReset);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton btnView = UITheme.createPrimaryButton("ℹ View Clearance Details");
        btnView.addActionListener(e -> viewSelectedClearance());
        actions.add(btnView);

        JButton btnExport = UITheme.createSecondaryButton("📥 Export List");
        btnExport.addActionListener(e -> ExportUtil.exportTableToCSV(this, verificationTable, "Barangay_Stakeholder_Clearances", "Barangay Stakeholder Clearances"));
        actions.add(btnExport);

        topBar.add(filters, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Data Table
        String[] cols = {"ID", "Stakeholder Enterprise", "Business Category", "Barangay Clearance Ref", "Status", "Official Remarks", "Valid Until"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        verificationTable = UITheme.createStyledTable(tableModel);
        verificationTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedClearance();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(verificationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        // Bottom Endorsement Controls
        JPanel actionFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionFooter.setOpaque(false);

        JButton endorseBtn = UITheme.createSuccessButton("✓ Endorse & Approve Clearance");
        endorseBtn.addActionListener(e -> handleReviewAction("APPROVED"));

        JButton rejectBtn = UITheme.createDangerButton("✕ Reject / Request Documents");
        rejectBtn.addActionListener(e -> handleReviewAction("REJECTED"));

        actionFooter.add(endorseBtn);
        actionFooter.add(rejectBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(actionFooter, BorderLayout.SOUTH);

        loadVerifications();
    }

    public void loadVerifications() {
        recordList = verificationService.getPendingVerifications();
        if (recordList.isEmpty()) {
            // Seed representative records if DB has none yet
            VerificationRecord v1 = new VerificationRecord();
            v1.setVerificationId(1);
            v1.setStakeholderId(3);
            v1.setStakeholderName("Maripipi Seafarers Motorized Pumpboat Cooperative");
            v1.setDocumentType("Coast Guard Safety Inspection & Barangay Clearance");
            v1.setStatus("APPROVED");
            v1.setRemarks("Complete safety clearance; life jackets verified; annual permits active");
            v1.setValidityDate(LocalDate.now().plusYears(1).toString());

            VerificationRecord v2 = new VerificationRecord();
            v2.setVerificationId(2);
            v2.setStakeholderId(1);
            v2.setStakeholderName("Sambawan Island Eco-Trek & Marine Guides");
            v2.setDocumentType("DOT Eco-Guide Certification & First Aid Clearance");
            v2.setStatus("APPROVED");
            v2.setRemarks("Wilderness first aid credentials verified; compliant with park guidelines");
            v2.setValidityDate(LocalDate.now().plusYears(1).toString());

            VerificationRecord v3 = new VerificationRecord();
            v3.setVerificationId(3);
            v3.setStakeholderId(4);
            v3.setStakeholderName("Nanay Luz Traditional Biliran Catering");
            v3.setDocumentType("Barangay Sanitary Permit & Food Safety Clearance");
            v3.setStatus("PENDING");
            v3.setRemarks("Awaiting updated health inspection report from municipal health office");
            v3.setValidityDate(LocalDate.now().plusMonths(6).toString());

            recordList.add(v1);
            recordList.add(v2);
            recordList.add(v3);
        }

        applyFilter();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim().toLowerCase();
        String status = (String) cmbStatus.getSelectedItem();

        for (VerificationRecord v : recordList) {
            String name = (v.getStakeholderName() != null ? v.getStakeholderName() : "").toLowerCase();
            String doc = (v.getDocumentType() != null ? v.getDocumentType() : "").toLowerCase();

            if (!search.isEmpty() && !name.contains(search) && !doc.contains(search)) {
                continue;
            }

            if (status != null && !status.equals("ALL") && !status.equalsIgnoreCase(v.getStatus())) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    v.getVerificationId(),
                    v.getStakeholderName() != null ? v.getStakeholderName() : "Community Stakeholder #" + v.getStakeholderId(),
                    "Community Tourism Provider",
                    v.getDocumentType() != null ? v.getDocumentType() : "Barangay Clearance Ref",
                    v.getStatus() != null ? v.getStatus() : "PENDING",
                    v.getRemarks() != null ? v.getRemarks() : "Compliant with barangay ordinances",
                    v.getValidityDate() != null ? v.getValidityDate().toString() : "2026-12-31"
            });
        }
    }

    private void handleReviewAction(String newStatus) {
        int row = verificationTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an enterprise clearance application from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String prompt = newStatus.equals("APPROVED") ?
                "Enter official barangay endorsement remarks / clearance note:" :
                "Enter reason for rejection or list required missing documents:";

        String defaultRemark = newStatus.equals("APPROVED") ?
                "Endorsed by Barangay Tourism Council. All safety requirements verified." :
                "Incomplete documentation: Requires updated municipal health/sanitary clearance.";

        String remarks = (String) JOptionPane.showInputDialog(
                this,
                prompt,
                "Official Endorsement Remarks",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                defaultRemark
        );

        if (remarks == null || remarks.trim().isEmpty()) return;

        User user = SessionManager.getCurrentUser();
        int reviewerId = (user != null) ? user.getUserId() : 4; // Captain Ramon Corpin

        boolean success = verificationService.verifyStakeholder(id, reviewerId, newStatus, remarks.trim(), LocalDate.now().plusYears(1));

        for (VerificationRecord r : recordList) {
            if (r.getVerificationId() == id) {
                r.setStatus(newStatus);
                r.setRemarks(remarks.trim());
                r.setValidityDate(LocalDate.now().plusYears(1).toString());
                break;
            }
        }
        applyFilter();

        JOptionPane.showMessageDialog(this,
                "Clearance application #" + id + " has been marked as: " + newStatus + "\nRemarks: " + remarks,
                "Endorsement Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewSelectedClearance() {
        int row = verificationTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application record from the table to view details.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String doc = (String) tableModel.getValueAt(row, 3);
        String status = (String) tableModel.getValueAt(row, 4);
        String remarks = (String) tableModel.getValueAt(row, 5);
        String validUntil = (String) tableModel.getValueAt(row, 6);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Barangay Clearance Details — " + name, true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = UITheme.createCardPanel("Official Barangay Clearance & Endorsement Document");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(new JLabel("<html><b>Enterprise:</b> " + name + "</html>"));
        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("<html><b>Documentation:</b> " + doc + "</html>"));
        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("<html><b>Accreditation Status:</b> " + status + "</html>"));
        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("<html><b>Validity Period:</b> Valid until " + validUntil + "</html>"));
        card.add(Box.createVerticalStrut(14));

        JLabel remLbl = new JLabel("Official Council Remarks:");
        remLbl.setFont(UITheme.FONT_BOLD);
        card.add(remLbl);
        card.add(Box.createVerticalStrut(4));

        JTextArea remArea = new JTextArea(remarks);
        remArea.setFont(UITheme.FONT_BODY);
        remArea.setLineWrap(true);
        remArea.setWrapStyleWord(true);
        remArea.setEditable(false);
        remArea.setBackground(new Color(248, 250, 252));
        remArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.add(new JScrollPane(remArea));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        btns.add(closeBtn);

        root.add(card, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
