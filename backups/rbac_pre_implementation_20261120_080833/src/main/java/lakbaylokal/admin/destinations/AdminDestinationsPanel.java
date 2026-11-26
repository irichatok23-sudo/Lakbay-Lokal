package lakbaylokal.admin.destinations;

import lakbaylokal.model.Destination;
import lakbaylokal.service.DestinationService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AdminDestinationsPanel extends JPanel {
    private final DestinationService destinationService = new DestinationService();

    private JTable destinationsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilterCombo;

    public AdminDestinationsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableSection(), BorderLayout.CENTER);

        loadDestinations();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Master Destination Catalog");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Full administrative oversight of tourist spots, eco-zones, fee schedules, and metadata.");
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
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(destinationsTable, "Admin_Destinations_Catalog"));

        JButton addBtn = UITheme.createSuccessButton("+ Add Destination");
        addBtn.addActionListener(e -> showAddDestinationDialog());

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadDestinations());

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
        searchField.putClientProperty("JTextField.placeholderText", "Spot or municipality...");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Category:"));
        categoryFilterCombo = new JComboBox<>(new String[]{"ALL", "Beach / Island", "Waterfalls", "Hot Spring", "Mountain / Hiking", "Cultural / Heritage"});
        filterBar.add(categoryFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.addActionListener(e -> loadDestinations());
        filterBar.add(filterBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilterCombo.setSelectedIndex(0);
            loadDestinations();
        });
        filterBar.add(resetBtn);

        // Table
        String[] cols = {"ID", "Destination Name", "Municipality", "Category", "Entrance Fee", "Promotion", "Status", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        destinationsTable = new JTable(tableModel);
        UITheme.styleTable(destinationsTable);

        JScrollPane scrollPane = new JScrollPane(destinationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        JButton editBtn = UITheme.createPrimaryButton("Edit Destination");
        editBtn.addActionListener(e -> editSelectedDestination());

        JButton toggleFeaturedBtn = UITheme.createWarningButton("Toggle Featured Highlight");
        toggleFeaturedBtn.addActionListener(e -> toggleFeatured());

        JButton deleteBtn = UITheme.createDangerButton("Delete Destination");
        deleteBtn.addActionListener(e -> deleteSelectedDestination());

        footer.add(editBtn);
        footer.add(toggleFeaturedBtn);
        footer.add(deleteBtn);

        container.add(filterBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    public void loadDestinations() {
        tableModel.setRowCount(0);
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String catFilter = (String) (categoryFilterCombo != null ? categoryFilterCombo.getSelectedItem() : "ALL");

        List<Destination> list = destinationService.getAllDestinations();
        for (Destination d : list) {
            if (catFilter != null && !catFilter.equalsIgnoreCase("ALL") && !catFilter.equalsIgnoreCase(d.getCategory())) {
                continue;
            }
            if (!kw.isEmpty()) {
                String combined = (d.getName() + " " + d.getMunicipalityName() + " " + d.getCategory() + " " + d.getDescription()).toLowerCase();
                if (!combined.contains(kw)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    d.getDestinationId(),
                    d.getName(),
                    d.getMunicipalityName() != null ? d.getMunicipalityName() : "Biliran",
                    d.getCategory(),
                    "₱" + d.getEntranceFee(),
                    d.isFeatured() ? "★ FEATURED" : "STANDARD",
                    d.getStatus() != null ? d.getStatus() : "ACTIVE",
                    d.getDescription()
            });
        }
    }

    private void showAddDestinationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Biliran Destination", true);
        dialog.setSize(540, 480);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField();
        JComboBox<String> munCombo = new JComboBox<>(new String[]{"Maripipi", "Caibiran", "Naval", "Almeria", "Kawayan", "Biliran", "Cabucgayan", "Culaba"});
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"Beach / Island", "Waterfalls", "Mountain / Hiking", "Hot Spring", "Cultural / Heritage"});
        JTextField feeField = new JTextField("100.00");
        JCheckBox featCheck = new JCheckBox("Highlight as Featured Destination", true);
        JTextField hoursField = new JTextField("6:00 AM - 5:00 PM");
        JTextField descField = new JTextField();

        form.add(new JLabel("Destination Name:"));
        form.add(nameField);
        form.add(new JLabel("Municipality:"));
        form.add(munCombo);
        form.add(new JLabel("Category:"));
        form.add(catCombo);
        form.add(new JLabel("Entrance / Eco Fee (PHP):"));
        form.add(feeField);
        form.add(new JLabel("Operating Hours:"));
        form.add(hoursField);
        form.add(new JLabel("Promotion:"));
        form.add(featCheck);
        form.add(new JLabel("Description:"));
        form.add(descField);

        JButton saveBtn = UITheme.createPrimaryButton("Save Destination");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal fee = new BigDecimal(feeField.getText().trim());

                Destination d = new Destination();
                d.setName(name);
                int munIndex = munCombo.getSelectedIndex() + 1; // 1 to 8
                d.setBarangayId(munIndex); // maps to seeded barangays 1 to 8
                d.setCategory((String) catCombo.getSelectedItem());
                d.setEntranceFee(fee);
                d.setOperatingHours(hoursField.getText().trim());
                d.setFeatured(featCheck.isSelected());
                d.setStatus("ACTIVE");
                d.setDescription(descField.getText().trim());

                int newId = destinationService.addDestination(d);
                if (newId > 0) {
                    dialog.dispose();
                    loadDestinations();
                    JOptionPane.showMessageDialog(this, "Destination created successfully! ID #" + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create destination in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid fee amount. Please enter numbers only.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(saveBtn);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(form, BorderLayout.CENTER);
        dialog.getContentPane().add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedDestination() {
        int row = destinationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a destination to edit.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Destination d = destinationService.getDestinationById(id);
        if (d == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Destination #" + id, true);
        dialog.setSize(540, 480);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField(d.getName());
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"Beach / Island", "Waterfalls", "Mountain / Hiking", "Hot Spring", "Cultural / Heritage"});
        catCombo.setSelectedItem(d.getCategory());
        JTextField feeField = new JTextField(d.getEntranceFee() != null ? d.getEntranceFee().toString() : "50.00");
        JTextField hoursField = new JTextField(d.getOperatingHours() != null ? d.getOperatingHours() : "6:00 AM - 5:00 PM");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "CLOSED", "MAINTENANCE"});
        if (d.getStatus() != null) statusCombo.setSelectedItem(d.getStatus());
        JCheckBox featCheck = new JCheckBox("Highlight as Featured", d.isFeatured());
        JTextField descField = new JTextField(d.getDescription() != null ? d.getDescription() : "");

        form.add(new JLabel("Destination Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(catCombo);
        form.add(new JLabel("Entrance Fee (PHP):"));
        form.add(feeField);
        form.add(new JLabel("Operating Hours:"));
        form.add(hoursField);
        form.add(new JLabel("Status:"));
        form.add(statusCombo);
        form.add(new JLabel("Promotion:"));
        form.add(featCheck);
        form.add(new JLabel("Description:"));
        form.add(descField);

        JButton saveBtn = UITheme.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            try {
                d.setName(nameField.getText().trim());
                d.setCategory((String) catCombo.getSelectedItem());
                d.setEntranceFee(new BigDecimal(feeField.getText().trim()));
                d.setOperatingHours(hoursField.getText().trim());
                d.setStatus((String) statusCombo.getSelectedItem());
                d.setFeatured(featCheck.isSelected());
                d.setDescription(descField.getText().trim());

                boolean ok = destinationService.updateDestination(d);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Destination updated in database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadDestinations();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update destination in database.", "Error", JOptionPane.ERROR_MESSAGE);
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
        dialog.getContentPane().add(form, BorderLayout.CENTER);
        dialog.getContentPane().add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void toggleFeatured() {
        int row = destinationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a destination first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Destination d = destinationService.getDestinationById(id);
        if (d != null) {
            boolean nextFeatured = !d.isFeatured();
            d.setFeatured(nextFeatured);
            boolean ok = destinationService.updateDestination(d);
            if (ok) {
                String next = nextFeatured ? "★ FEATURED" : "STANDARD";
                tableModel.setValueAt(next, row, 5);
                JOptionPane.showMessageDialog(this, "Destination '" + d.getName() + "' featured highlight updated to: " + next, "Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update destination in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedDestination() {
        int row = destinationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a destination to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + name + "' (ID #" + id + ") from the database?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = destinationService.deleteDestination(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Destination deleted successfully from database.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadDestinations();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete destination. Associated arrival logs or packages may exist.", "Database Constraint", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
