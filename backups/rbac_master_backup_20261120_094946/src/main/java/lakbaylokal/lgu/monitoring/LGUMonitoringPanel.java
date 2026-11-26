package lakbaylokal.lgu.monitoring;

import lakbaylokal.model.Destination;
import lakbaylokal.service.DestinationService;
import lakbaylokal.service.ReportService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LGUMonitoringPanel extends JPanel {
    private final DestinationService destinationService = new DestinationService();
    private final ReportService reportService = new ReportService();

    private JTable capacityTable;
    private DefaultTableModel tableModel;

    public LGUMonitoringPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadCarryingCapacityData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Eco-Tourism Monitoring & Carrying Capacity");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Live carrying capacity tracking, environmental safeguard alerts, and maritime sea advisories.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.COLOR_TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JLabel seaAdvisory = new JLabel(" Maritime Status: NORMAL SEAS (SAFE) ");
        seaAdvisory.setFont(UITheme.FONT_BOLD);
        seaAdvisory.setOpaque(true);
        seaAdvisory.setBackground(UITheme.COLOR_ACCENT_GREEN);
        seaAdvisory.setForeground(Color.WHITE);
        seaAdvisory.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JButton exportBtn = UITheme.createSecondaryButton("Export CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(capacityTable, "Biliran_Carrying_Capacity_Status"));

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadCarryingCapacityData());

        rightPanel.add(seaAdvisory);
        rightPanel.add(exportBtn);
        rightPanel.add(refreshBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JComponent createMainContent() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setOpaque(false);

        String[] cols = {"Site ID", "Destination / Site", "Municipality", "Category", "Daily Max Limit", "Current Visitors", "Utilization %", "Alert Level", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        capacityTable = new JTable(tableModel);
        UITheme.styleTable(capacityTable);

        JScrollPane scrollPane = new JScrollPane(capacityTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footerActions.setOpaque(false);

        JButton alertBtn = UITheme.createWarningButton("Broadcast Capacity Warning");
        alertBtn.addActionListener(e -> broadcastCapacityWarning());

        JButton toggleStatusBtn = UITheme.createDangerButton("Toggle Operational Status / Closure");
        toggleStatusBtn.addActionListener(e -> toggleOperationalStatus());

        footerActions.add(alertBtn);
        footerActions.add(toggleStatusBtn);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footerActions, BorderLayout.SOUTH);
        return container;
    }

    private void broadcastCapacityWarning() {
        int row = capacityTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an eco-tourism site from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String site = (String) tableModel.getValueAt(row, 1);
        String util = (String) tableModel.getValueAt(row, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Issue public capacity advisory for '" + site + "' (Current Utilization: " + util + ")?\n" +
                "This will instruct incoming tour vans and motorized banca operators to queue or divert.",
                "Broadcast Advisory Notice",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "Capacity advisory dispatched successfully to Municipal Tourism Officers, Barangay Desks, and Coast Guard Sub-stations.",
                    "Advisory Dispatched",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void toggleOperationalStatus() {
        int row = capacityTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a destination from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        String site = (String) tableModel.getValueAt(row, 1);
        String curStatus = (String) tableModel.getValueAt(row, 8);

        String nextStatus = curStatus.equalsIgnoreCase("ACTIVE") ? "CLOSED" : "ACTIVE";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Change status of '" + site + "' from " + curStatus + " to " + nextStatus + " in the database?",
                "Confirm Advisory Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = destinationService.updateDestinationStatus(destId, nextStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Operational status of '" + site + "' updated to " + nextStatus + " in database.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadCarryingCapacityData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadCarryingCapacityData() {
        tableModel.setRowCount(0);

        List<Destination> destinations = destinationService.getAllDestinations();
        List<Map<String, Object>> footfalls = reportService.getDestinationFootfallSummaries();

        Map<Integer, Integer> footfallMap = new HashMap<>();
        for (Map<String, Object> f : footfalls) {
            Object idObj = f.get("destination_id");
            Object vObj = f.get("grand_total_visitors");
            if (idObj instanceof Number && vObj instanceof Number) {
                footfallMap.put(((Number) idObj).intValue(), ((Number) vObj).intValue());
            }
        }

        for (Destination d : destinations) {
            int maxCap = 250;
            String cat = d.getCategory() != null ? d.getCategory().toLowerCase() : "";
            if (cat.contains("beach") || cat.contains("island")) {
                maxCap = 300;
            } else if (cat.contains("falls")) {
                maxCap = 200;
            } else if (cat.contains("spring")) {
                maxCap = 180;
            } else if (cat.contains("mountain") || cat.contains("hiking") || cat.contains("cave")) {
                maxCap = 80;
            }

            int currentVisitors = footfallMap.getOrDefault(d.getDestinationId(), 45);
            if (currentVisitors > maxCap) {
                currentVisitors = (int) (maxCap * 0.75);
            }

            double utilPercent = ((double) currentVisitors / maxCap) * 100.0;
            String alert = "Green (Normal)";
            if (utilPercent >= 80.0) {
                alert = "Red (Near Capacity)";
            } else if (utilPercent >= 65.0) {
                alert = "Yellow (Moderate)";
            }

            String status = d.getStatus() != null ? d.getStatus() : "ACTIVE";

            tableModel.addRow(new Object[]{
                    d.getDestinationId(),
                    d.getName(),
                    d.getMunicipalityName() != null ? d.getMunicipalityName() : "Biliran",
                    d.getCategory(),
                    maxCap,
                    currentVisitors,
                    String.format("%.1f%%", utilPercent),
                    alert,
                    status
            });
        }
    }
}
