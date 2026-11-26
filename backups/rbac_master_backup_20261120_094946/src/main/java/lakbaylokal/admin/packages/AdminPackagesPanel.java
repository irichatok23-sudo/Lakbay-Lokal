package lakbaylokal.admin.packages;

import lakbaylokal.model.TourismPackage;
import lakbaylokal.service.PackageService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AdminPackagesPanel extends JPanel {
    private final PackageService packageService = new PackageService();

    private JTable packagesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;

    public AdminPackagesPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableSection(), BorderLayout.CENTER);

        loadPackages();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Community Tourism Packages Master List");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage bundled community experiences, day tours, rates, and participant quotas.");
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

        JButton exportBtn = UITheme.createSecondaryButton("Export CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(packagesTable, "Admin_Packages_Catalog"));

        JButton addBtn = UITheme.createSuccessButton("+ Add New Package");
        addBtn.addActionListener(e -> showAddPackageDialog());

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadPackages());

        actionPanel.add(exportBtn);
        actionPanel.add(addBtn);
        actionPanel.add(refreshBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);
        return header;
    }

    private JComponent createTableSection() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setOpaque(false);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("Search:"));
        searchField = new JTextField(16);
        searchField.putClientProperty("JTextField.placeholderText", "Package title...");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "ACTIVE", "INACTIVE"});
        filterBar.add(statusFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.addActionListener(e -> loadPackages());
        filterBar.add(filterBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadPackages();
        });
        filterBar.add(resetBtn);

        // Table
        String[] cols = {"Package ID", "Title / Name", "Duration", "Price (PHP)", "Max Participants", "Status", "Inclusions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        packagesTable = new JTable(tableModel);
        UITheme.styleTable(packagesTable);

        JScrollPane scrollPane = new JScrollPane(packagesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // Action Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        JButton editBtn = UITheme.createPrimaryButton("Edit Package");
        editBtn.addActionListener(e -> editSelectedPackage());

        JButton toggleBtn = UITheme.createWarningButton("Toggle Active / Inactive");
        toggleBtn.addActionListener(e -> togglePackageStatus());

        JButton deleteBtn = UITheme.createDangerButton("Delete Package");
        deleteBtn.addActionListener(e -> deleteSelectedPackage());

        footer.add(editBtn);
        footer.add(toggleBtn);
        footer.add(deleteBtn);

        container.add(filterBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    public void loadPackages() {
        tableModel.setRowCount(0);
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = (String) (statusFilterCombo != null ? statusFilterCombo.getSelectedItem() : "ALL");

        List<TourismPackage> list = packageService.getAllPackages();
        for (TourismPackage p : list) {
            String status = p.isActive() ? "ACTIVE" : "INACTIVE";
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL") && !statusFilter.equalsIgnoreCase(status)) {
                continue;
            }
            if (!kw.isEmpty()) {
                String combined = (p.getPackageName() + " " + p.getDescription() + " " + p.getInclusions()).toLowerCase();
                if (!combined.contains(kw)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    p.getPackageId(),
                    p.getPackageName(),
                    p.getDurationDays() + " Day(s)",
                    "₱" + p.getPrice(),
                    p.getMaxPax() + " pax",
                    status,
                    p.getInclusions() != null ? p.getInclusions() : "Local guides, transfers, entrance fees"
            });
        }
    }

    private void showAddPackageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Community Tourism Package", true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField();
        JTextField durationField = new JTextField("1");
        JTextField priceField = new JTextField("2500.00");
        JTextField maxPaxField = new JTextField("8");
        JTextField descField = new JTextField();
        JTextField inclField = new JTextField("Boat, Guide, Lunch, Environmental Fee");

        panel.add(new JLabel("Package Title:"));
        panel.add(nameField);
        panel.add(new JLabel("Duration (Days):"));
        panel.add(durationField);
        panel.add(new JLabel("Price (PHP):"));
        panel.add(priceField);
        panel.add(new JLabel("Max Participants:"));
        panel.add(maxPaxField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Inclusions:"));
        panel.add(inclField);

        JButton saveBtn = UITheme.createPrimaryButton("Save Package");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Package title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                TourismPackage pkg = new TourismPackage();
                pkg.setPackageName(name);
                pkg.setName(name);
                pkg.setDurationDays(Integer.parseInt(durationField.getText().trim()));
                pkg.setPrice(new BigDecimal(priceField.getText().trim()));
                pkg.setPricePerPax(new BigDecimal(priceField.getText().trim()));
                pkg.setMinPax(1);
                pkg.setMaxPax(Integer.parseInt(maxPaxField.getText().trim()));
                pkg.setDescription(descField.getText().trim());
                pkg.setInclusions(inclField.getText().trim());
                pkg.setActive(true);
                pkg.setStatus("ACTIVE");
                pkg.setPackageType("COMMUNITY_TOUR");

                int newId = packageService.createPackage(pkg);
                if (newId > 0) {
                    dialog.dispose();
                    loadPackages();
                    JOptionPane.showMessageDialog(this, "Package created successfully! ID #" + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save package in database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(saveBtn);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedPackage() {
        int row = packagesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a package to edit.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        TourismPackage pkg = packageService.getPackageById(id);
        if (pkg == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Package #" + id, true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField(pkg.getPackageName() != null ? pkg.getPackageName() : pkg.getName());
        JTextField durationField = new JTextField(String.valueOf(pkg.getDurationDays()));
        JTextField priceField = new JTextField(pkg.getPrice() != null ? pkg.getPrice().toString() : "2500.00");
        JTextField maxPaxField = new JTextField(String.valueOf(pkg.getMaxPax()));
        JTextField descField = new JTextField(pkg.getDescription() != null ? pkg.getDescription() : "");
        JTextField inclField = new JTextField(pkg.getInclusions() != null ? pkg.getInclusions() : "");

        panel.add(new JLabel("Package Title:"));
        panel.add(nameField);
        panel.add(new JLabel("Duration (Days):"));
        panel.add(durationField);
        panel.add(new JLabel("Price (PHP):"));
        panel.add(priceField);
        panel.add(new JLabel("Max Participants:"));
        panel.add(maxPaxField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Inclusions:"));
        panel.add(inclField);

        JButton saveBtn = UITheme.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            try {
                pkg.setPackageName(nameField.getText().trim());
                pkg.setName(nameField.getText().trim());
                pkg.setDurationDays(Integer.parseInt(durationField.getText().trim()));
                pkg.setPrice(new BigDecimal(priceField.getText().trim()));
                pkg.setPricePerPax(new BigDecimal(priceField.getText().trim()));
                pkg.setMaxPax(Integer.parseInt(maxPaxField.getText().trim()));
                pkg.setDescription(descField.getText().trim());
                pkg.setInclusions(inclField.getText().trim());

                boolean ok = packageService.updatePackage(pkg);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Package updated in database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadPackages();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update package in database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid numeric input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(saveBtn);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void togglePackageStatus() {
        int row = packagesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a package first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        TourismPackage pkg = packageService.getPackageById(id);
        if (pkg != null) {
            boolean nextActive = !pkg.isActive();
            pkg.setActive(nextActive);
            pkg.setStatus(nextActive ? "ACTIVE" : "INACTIVE");

            boolean ok = packageService.updatePackage(pkg);
            if (ok) {
                String next = nextActive ? "ACTIVE" : "INACTIVE";
                tableModel.setValueAt(next, row, 5);
                JOptionPane.showMessageDialog(this, "Package status updated to: " + next + " in database.", "Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update package in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedPackage() {
        int row = packagesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a package to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete package '" + title + "' (ID #" + id + ") from the database?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = packageService.deletePackage(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Package deleted successfully from database.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadPackages();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete package. Bookings may be referencing it.", "Database Constraint", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
