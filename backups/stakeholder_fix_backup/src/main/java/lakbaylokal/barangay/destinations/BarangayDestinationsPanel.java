package lakbaylokal.barangay.destinations;

import lakbaylokal.model.ArrivalRecord;
import lakbaylokal.model.Destination;
import lakbaylokal.service.DestinationService;
import lakbaylokal.service.ReportService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class BarangayDestinationsPanel extends JPanel {
    private final DestinationService destinationService = new DestinationService();
    private final ReportService reportService = new ReportService();

    private JTable destinationsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JLabel totalSpotsLabel;
    private JLabel activeSpotsLabel;
    private JLabel advisorySpotsLabel;

    public BarangayDestinationsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);

        loadDestinations();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Barangay Tourism Spots & Attractions");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Jurisdictional monitoring of local eco-tourism sites, carrying capacities, and environmental fees.");
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
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(destinationsTable, "Barangay_Tourism_Spots"));

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh Spots");
        refreshBtn.addActionListener(e -> loadDestinations());

        actions.add(exportBtn);
        actions.add(refreshBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);

        // Stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 15));
        statsRow.setOpaque(false);

        totalSpotsLabel = new JLabel("0");
        totalSpotsLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalSpotsLabel.setForeground(UITheme.COLOR_PRIMARY);

        activeSpotsLabel = new JLabel("0");
        activeSpotsLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        activeSpotsLabel.setForeground(UITheme.COLOR_ACCENT_GREEN);

        advisorySpotsLabel = new JLabel("0");
        advisorySpotsLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        advisorySpotsLabel.setForeground(UITheme.COLOR_ACCENT_ORANGE);

        statsRow.add(UITheme.createStatCard("Total Attractions", totalSpotsLabel, "Registered in Biliran"));
        statsRow.add(UITheme.createStatCard("Open / Active", activeSpotsLabel, "Welcoming tourists"));
        statsRow.add(UITheme.createStatCard("Advisory / Closed", advisorySpotsLabel, "Maintenance or weather hold"));

        // Table and filter container
        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));
        tableContainer.setOpaque(false);

        // Filter toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("Search:"));
        searchField = new JTextField(16);
        searchField.putClientProperty("JTextField.placeholderText", "Name or municipality...");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"ALL", "Beach / Island", "Waterfalls", "Hot Spring", "Mountain / Hiking", "Cultural / Heritage"});
        filterBar.add(categoryCombo);

        JButton searchBtn = UITheme.createPrimaryButton("Filter");
        searchBtn.addActionListener(e -> loadDestinations());
        filterBar.add(searchBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryCombo.setSelectedIndex(0);
            loadDestinations();
        });
        filterBar.add(resetBtn);

        // Table
        String[] cols = {"ID", "Attraction Name", "Category", "Municipality", "Barangay", "Entrance / Eco-Fee", "Rating", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        destinationsTable = new JTable(tableModel);
        UITheme.styleTable(destinationsTable);

        JScrollPane scrollPane = new JScrollPane(destinationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // Action Toolbar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footer.setOpaque(false);

        JButton toggleOpenBtn = UITheme.createWarningButton("Toggle Site Advisory (Active/Closed)");
        toggleOpenBtn.addActionListener(e -> toggleSiteStatus());

        JButton logArrivalBtn = UITheme.createPrimaryButton("Log Tourist Arrival / Check-in");
        logArrivalBtn.addActionListener(e -> openLogArrivalDialog());

        JButton viewDetailsBtn = UITheme.createSecondaryButton("View Attraction Details");
        viewDetailsBtn.addActionListener(e -> viewDetails());

        footer.add(toggleOpenBtn);
        footer.add(logArrivalBtn);
        footer.add(viewDetailsBtn);

        tableContainer.add(filterBar, BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        tableContainer.add(footer, BorderLayout.SOUTH);

        center.add(statsRow, BorderLayout.NORTH);
        center.add(tableContainer, BorderLayout.CENTER);
        return center;
    }

    private void toggleSiteStatus() {
        int row = destinationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attraction from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String curStatus = (String) tableModel.getValueAt(row, 7);

        String[] options = {"ACTIVE", "CLOSED", "MAINTENANCE"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select new operational status for '" + name + "':",
                "Update Site Advisory",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                curStatus.equalsIgnoreCase("ACTIVE") ? "CLOSED" : "ACTIVE");

        if (selected != null && !selected.isBlank() && !selected.equalsIgnoreCase(curStatus)) {
            boolean success = destinationService.updateDestinationStatus(destId, selected);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Operational advisory for '" + name + "' updated to " + selected + " in database.",
                        "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadDestinations();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update destination advisory in database.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openLogArrivalDialog() {
        int row = destinationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select the destination where tourists arrived.", "Select Destination", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        String destName = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Log Tourist Arrival - " + destName, true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField localField = new JTextField("0");
        JTextField domesticField = new JTextField("1");
        JTextField foreignField = new JTextField("0");
        JTextField revenueField = new JTextField("50.00");

        form.add(new JLabel("Destination:"));
        form.add(new JLabel("<html><b>" + destName + "</b></html>"));
        form.add(new JLabel("Local Tourists (Biliran):"));
        form.add(localField);
        form.add(new JLabel("Domestic Tourists (PH):"));
        form.add(domesticField);
        form.add(new JLabel("Foreign Tourists:"));
        form.add(foreignField);
        form.add(new JLabel("Eco / Entrance Fee Collected (₱):"));
        form.add(revenueField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = UITheme.createPrimaryButton("Save Log Record");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");

        saveBtn.addActionListener(ev -> {
            try {
                int localCount = Integer.parseInt(localField.getText().trim());
                int domesticCount = Integer.parseInt(domesticField.getText().trim());
                int foreignCount = Integer.parseInt(foreignField.getText().trim());
                BigDecimal rev = new BigDecimal(revenueField.getText().trim());

                if (localCount < 0 || domesticCount < 0 || foreignCount < 0 || (localCount + domesticCount + foreignCount) == 0) {
                    JOptionPane.showMessageDialog(dialog, "Total visitors must be at least 1.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ArrivalRecord ar = new ArrivalRecord();
                ar.setDestinationId(destId);
                ar.setLogDate(Date.valueOf(LocalDate.now()));
                ar.setLocalTouristsCount(localCount);
                ar.setDomesticTouristsCount(domesticCount);
                ar.setForeignTouristsCount(foreignCount);
                ar.setTotalRevenue(rev);
                ar.setRecordedBy(4); // Default Barangay Officer ID

                int logId = reportService.recordArrival(ar);
                if (logId > 0) {
                    JOptionPane.showMessageDialog(dialog, "Visitor arrival record logged successfully! ID #" + logId, "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to record arrival in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric figures.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewDetails() {
        int row = destinationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attraction from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        Destination d = destinationService.getDestinationById(destId);
        if (d == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Attraction Details: " + d.getName(), true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(8, 2, 10, 8));
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        p.add(new JLabel("Name:"));
        p.add(new JLabel("<html><b>" + d.getName() + "</b></html>"));
        p.add(new JLabel("Category:"));
        p.add(new JLabel(d.getCategory()));
        p.add(new JLabel("Municipality:"));
        p.add(new JLabel(d.getMunicipalityName() != null ? d.getMunicipalityName() : "Biliran"));
        p.add(new JLabel("Barangay:"));
        p.add(new JLabel(d.getBarangayName() != null ? d.getBarangayName() : "Poblacion"));
        p.add(new JLabel("Entrance Fee:"));
        p.add(new JLabel("₱" + d.getEntranceFee()));
        p.add(new JLabel("Operating Hours:"));
        p.add(new JLabel(d.getOperatingHours() != null ? d.getOperatingHours() : "6:00 AM - 5:00 PM"));
        p.add(new JLabel("Average Rating:"));
        p.add(new JLabel(d.getAverageRating() + " ★ (" + d.getReviewCount() + " reviews)"));
        p.add(new JLabel("Status:"));
        p.add(new JLabel(d.getStatus()));

        JTextArea descArea = new JTextArea(d.getDescription() != null ? d.getDescription() : "No description provided.");
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBorder(BorderFactory.createTitledBorder("Description & Carrying Advisory"));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(p, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(closeBtn);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void loadDestinations() {
        tableModel.setRowCount(0);
        String catFilter = (String) (categoryCombo != null ? categoryCombo.getSelectedItem() : "ALL");
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        List<Destination> list = destinationService.getAllDestinations();
        int total = 0;
        int active = 0;
        int advisory = 0;

        for (Destination d : list) {
            if (catFilter != null && !catFilter.equalsIgnoreCase("ALL") && !catFilter.equalsIgnoreCase(d.getCategory())) {
                continue;
            }
            if (!kw.isEmpty()) {
                String combined = (d.getName() + " " + d.getMunicipalityName() + " " + d.getBarangayName() + " " + d.getCategory()).toLowerCase();
                if (!combined.contains(kw)) {
                    continue;
                }
            }

            total++;
            String status = d.getStatus() != null ? d.getStatus() : "ACTIVE";
            if (status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("OPEN")) {
                active++;
            } else {
                advisory++;
            }

            tableModel.addRow(new Object[]{
                    d.getDestinationId(),
                    d.getName(),
                    d.getCategory(),
                    d.getMunicipalityName() != null ? d.getMunicipalityName() : "Biliran",
                    d.getBarangayName() != null ? d.getBarangayName() : "Poblacion",
                    "₱" + d.getEntranceFee(),
                    d.getAverageRating() + " ★",
                    status
            });
        }

        if (totalSpotsLabel != null) totalSpotsLabel.setText(String.valueOf(total));
        if (activeSpotsLabel != null) activeSpotsLabel.setText(String.valueOf(active));
        if (advisorySpotsLabel != null) advisorySpotsLabel.setText(String.valueOf(advisory));
    }
}
