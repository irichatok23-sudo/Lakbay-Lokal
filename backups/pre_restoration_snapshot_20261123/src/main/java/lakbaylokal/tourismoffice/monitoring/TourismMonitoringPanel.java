package lakbaylokal.tourismoffice.monitoring;

import lakbaylokal.model.Destination;
import lakbaylokal.service.DestinationService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TourismMonitoringPanel extends JPanel {
    private final DestinationService destinationService = new DestinationService();

    private JTable monitoringTable;
    private DefaultTableModel tableModel;

    public TourismMonitoringPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadMonitoringData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Provincial Tourism Standards & Influx Monitoring");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Monitor destination health, visitor satisfaction metrics, and provincial carrying compliance.");
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
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(monitoringTable, "DOT_Destination_Standards_Monitoring"));

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh Metrics");
        refreshBtn.addActionListener(e -> loadMonitoringData());

        actions.add(exportBtn);
        actions.add(refreshBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent createMainContent() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setOpaque(false);

        String[] cols = {"Destination ID", "Destination Name", "Municipality", "Category", "Avg Rating", "Fee", "DOT Compliance", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        monitoringTable = new JTable(tableModel);
        UITheme.styleTable(monitoringTable);

        JScrollPane scrollPane = new JScrollPane(monitoringTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footer.setOpaque(false);

        JButton inspectBtn = UITheme.createPrimaryButton("Schedule Field Quality Inspection");
        inspectBtn.addActionListener(e -> openScheduleInspectionDialog());

        JButton toggleStatusBtn = UITheme.createWarningButton("Toggle Operational Advisory");
        toggleStatusBtn.addActionListener(e -> toggleStatus());

        footer.add(inspectBtn);
        footer.add(toggleStatusBtn);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private void openScheduleInspectionDialog() {
        int row = monitoringTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a destination from the list first.", "Select Destination", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Schedule DOT Inspection: " + name, true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField inspectorField = new JTextField("Elena Cruz (Provincial Officer)");
        JTextField dateField = new JTextField(LocalDate.now().plusDays(3).toString());
        JComboBox<String> focusCombo = new JComboBox<>(new String[]{
                "DOT Safety & Life Vest Compliance",
                "Sanitation & Solid Waste Audit",
                "Community Homestay Standard Verification",
                "Eco-Trail Carrying Capacity Re-evaluation"
        });

        panel.add(new JLabel("Destination:"));
        panel.add(new JLabel("<html><b>" + name + "</b> (ID #" + destId + ")</html>"));
        panel.add(new JLabel("Lead Inspector:"));
        panel.add(inspectorField);
        panel.add(new JLabel("Inspection Target Date:"));
        panel.add(dateField);
        panel.add(new JLabel("Inspection Scope:"));
        panel.add(focusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = UITheme.createPrimaryButton("Confirm Schedule");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");

        saveBtn.addActionListener(ev -> {
            String insp = inspectorField.getText().trim();
            String dt = dateField.getText().trim();
            if (insp.isEmpty() || dt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all inspection details.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(dialog,
                    "Field Quality Inspection for '" + name + "' scheduled for " + dt + ".\nLead Inspector: " + insp + "\nNotification sent to Barangay Tourism Desk.",
                    "Inspection Scheduled",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void toggleStatus() {
        int row = monitoringTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attraction from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String curStatus = (String) tableModel.getValueAt(row, 7);

        String nextStatus = curStatus.equalsIgnoreCase("ACTIVE") ? "CLOSED" : "ACTIVE";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Change operational status of '" + name + "' from " + curStatus + " to " + nextStatus + " in the database?",
                "Confirm Advisory Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = destinationService.updateDestinationStatus(destId, nextStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Operational status of '" + name + "' updated to " + nextStatus + " in database.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadMonitoringData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadMonitoringData() {
        tableModel.setRowCount(0);
        List<Destination> list = destinationService.getAllDestinations();
        for (Destination d : list) {
            String compliance = "DOT Standard Compliant";
            if (d.getAverageRating() < 4.0 && d.getReviewCount() > 0) {
                compliance = "Under Quality Review";
            }

            tableModel.addRow(new Object[]{
                    d.getDestinationId(),
                    d.getName(),
                    d.getMunicipalityName() != null ? d.getMunicipalityName() : "Biliran",
                    d.getCategory(),
                    String.format("%.1f ★ (%d)", d.getAverageRating(), d.getReviewCount()),
                    "₱" + d.getEntranceFee(),
                    compliance,
                    d.getStatus() != null ? d.getStatus() : "ACTIVE"
            });
        }
    }
}
