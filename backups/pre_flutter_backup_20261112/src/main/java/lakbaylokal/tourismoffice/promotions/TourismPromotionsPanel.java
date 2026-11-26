package lakbaylokal.tourismoffice.promotions;

import lakbaylokal.model.Announcement;
import lakbaylokal.service.AnnouncementService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class TourismPromotionsPanel extends JPanel {
    private final AnnouncementService announcementService = new AnnouncementService();

    private JTable announcementsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;

    public TourismPromotionsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadAnnouncements();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Official Tourism Campaigns & Advisories");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Publish province-wide travel advisories, community festival notices, and seasonal eco-tour packages.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.COLOR_TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton exportBtn = UITheme.createSecondaryButton("Export CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(announcementsTable, "Biliran_Tourism_Campaigns"));

        JButton postBtn = UITheme.createPrimaryButton("+ Post New Campaign / Advisory");
        postBtn.addActionListener(e -> showAddAnnouncementDialog());

        actions.add(exportBtn);
        actions.add(postBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent createMainContent() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setOpaque(false);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("Search:"));
        searchField = new JTextField(16);
        searchField.putClientProperty("JTextField.placeholderText", "Title or keywords...");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PUBLISHED", "ARCHIVED"});
        filterBar.add(statusFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.addActionListener(e -> loadAnnouncements());
        filterBar.add(filterBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadAnnouncements();
        });
        filterBar.add(resetBtn);

        // Table
        String[] cols = {"ID", "Campaign / Advisory Title", "Category / Target", "Status", "Date Posted", "Content Snippet"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        announcementsTable = new JTable(tableModel);
        UITheme.styleTable(announcementsTable);

        JScrollPane scrollPane = new JScrollPane(announcementsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // Action Toolbar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        JButton toggleStatusBtn = UITheme.createWarningButton("Archive / Unpublish");
        toggleStatusBtn.addActionListener(e -> toggleAnnouncementStatus());

        JButton editBtn = UITheme.createSecondaryButton("Edit Notice");
        editBtn.addActionListener(e -> editAnnouncement());

        JButton deleteBtn = UITheme.createDangerButton("Delete Notice");
        deleteBtn.addActionListener(e -> deleteAnnouncement());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadAnnouncements());

        footer.add(toggleStatusBtn);
        footer.add(editBtn);
        footer.add(deleteBtn);
        footer.add(refreshBtn);

        container.add(filterBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private void toggleAnnouncementStatus() {
        int row = announcementsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an announcement from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        String cur = (String) tableModel.getValueAt(row, 3);
        String next = cur.equalsIgnoreCase("PUBLISHED") ? "ARCHIVED" : "PUBLISHED";

        Announcement a = announcementService.getAnnouncementById(id);
        if (a != null) {
            a.setStatus(next);
            boolean ok = announcementService.updateAnnouncement(a);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Notice '" + title + "' status updated to " + next + " in database.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAnnouncements();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update notice in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editAnnouncement() {
        int row = announcementsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an announcement to edit.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Announcement a = announcementService.getAnnouncementById(id);
        if (a == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Notice / Advisory #" + id, true);
        dialog.setSize(520, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField titleField = new JTextField(a.getTitle());
        JComboBox<String> targetCombo = new JComboBox<>(new String[]{"ALL", "TOURISTS", "STAKEHOLDERS", "BARANGAY"});
        if (a.getCategory() != null) targetCombo.setSelectedItem(a.getCategory());

        top.add(new JLabel("Campaign / Notice Title:"));
        top.add(titleField);
        top.add(new JLabel("Target Category:"));
        top.add(targetCombo);

        JTextArea bodyArea = new JTextArea(a.getContent(), 6, 25);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);

        JButton saveBtn = UITheme.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String b = bodyArea.getText().trim();
            if (t.isEmpty() || b.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in title and content.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            a.setTitle(t);
            a.setContent(b);
            a.setCategory((String) targetCombo.getSelectedItem());

            boolean ok = announcementService.updateAnnouncement(a);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Notice updated successfully in database!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadAnnouncements();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update notice in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(saveBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(bodyScroll, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void deleteAnnouncement() {
        int row = announcementsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an announcement to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete notice '" + title + "' (ID #" + id + ") from the database?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = announcementService.deleteAnnouncement(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Notice deleted successfully from database.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadAnnouncements();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete notice from database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadAnnouncements() {
        tableModel.setRowCount(0);
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = (String) (statusFilterCombo != null ? statusFilterCombo.getSelectedItem() : "ALL");

        List<Announcement> list = announcementService.getAllAnnouncements();
        for (Announcement a : list) {
            String status = a.getStatus() != null ? a.getStatus() : (a.isActive() ? "PUBLISHED" : "ARCHIVED");
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL") && !statusFilter.equalsIgnoreCase(status)) {
                continue;
            }
            if (!kw.isEmpty()) {
                String combined = (a.getTitle() + " " + a.getContent() + " " + a.getCategory()).toLowerCase();
                if (!combined.contains(kw)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    a.getAnnouncementId(),
                    a.getTitle(),
                    a.getCategory() != null ? a.getCategory() : "ALL",
                    status,
                    a.getPublishDate() != null ? a.getPublishDate().toString() : "2026-10-12",
                    a.getContent()
            });
        }
    }

    private void showAddAnnouncementDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Publish New Campaign or Advisory", true);
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField titleField = new JTextField();
        JComboBox<String> targetCombo = new JComboBox<>(new String[]{"ALL", "TOURISTS", "STAKEHOLDERS", "BARANGAY"});

        top.add(new JLabel("Campaign / Notice Title:"));
        top.add(titleField);
        top.add(new JLabel("Target Category:"));
        top.add(targetCombo);

        JTextArea bodyArea = new JTextArea(6, 25);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);

        JButton saveBtn = UITheme.createPrimaryButton("Publish to Biliran Portal");
        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String b = bodyArea.getText().trim();
            if (t.isEmpty() || b.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in title and content.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Announcement a = new Announcement();
            a.setTitle(t);
            a.setContent(b);
            a.setCategory((String) targetCombo.getSelectedItem());
            a.setStatus("PUBLISHED");
            a.setActive(true);
            a.setPublishedBy(2); // Provincial Tourism Office user_id = 2
            a.setPublishDate(Date.valueOf(LocalDate.now()));

            int id = announcementService.createAnnouncement(a);
            if (id > 0) {
                dialog.dispose();
                loadAnnouncements();
                JOptionPane.showMessageDialog(this, "Tourism campaign published successfully! ID #" + id, "Published", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save campaign to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(saveBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(bodyScroll, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
