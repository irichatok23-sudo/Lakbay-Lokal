package lakbaylokal.barangay.reports;

import lakbaylokal.model.ArrivalRecord;
import lakbaylokal.service.ReportService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class BarangayReportsPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private JLabel totalArrivalsLabel;
    private JLabel verifiedEnterprisesLabel;
    private JLabel ecoFeeLabel;
    private JLabel complianceRateLabel;

    private JTable arrivalLogsTable;
    private DefaultTableModel tableModel;

    public BarangayReportsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadStats();
        loadArrivalLogs();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Barangay Community Tourism Reports");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Local tourism indicators, community livelihood revenue, and environmental compliance.");
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

        JButton exportLogsBtn = UITheme.createSecondaryButton("Export Logs CSV");
        exportLogsBtn.addActionListener(e -> ExportUtil.exportTableToCSV(arrivalLogsTable, "Barangay_Arrival_Logs"));

        JButton exportBtn = UITheme.createPrimaryButton("Generate Official Summary");
        exportBtn.addActionListener(e -> exportOfficialSummary());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadStats();
            loadArrivalLogs();
        });

        actions.add(exportLogsBtn);
        actions.add(exportBtn);
        actions.add(refreshBtn);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent createBody() {
        JPanel body = new JPanel(new BorderLayout(15, 15));
        body.setOpaque(false);

        // Top Stat Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        statsGrid.setOpaque(false);

        totalArrivalsLabel = new JLabel("0");
        totalArrivalsLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        totalArrivalsLabel.setForeground(UITheme.COLOR_PRIMARY);

        verifiedEnterprisesLabel = new JLabel("0");
        verifiedEnterprisesLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        verifiedEnterprisesLabel.setForeground(UITheme.COLOR_ACCENT_GREEN);

        ecoFeeLabel = new JLabel("₱0.00");
        ecoFeeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        ecoFeeLabel.setForeground(UITheme.COLOR_ACCENT_ORANGE);

        complianceRateLabel = new JLabel("98.5%");
        complianceRateLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        complianceRateLabel.setForeground(new Color(13, 148, 136));

        statsGrid.add(createStatCard("Monthly Visitors", totalArrivalsLabel, "Tourists logged via check-ins"));
        statsGrid.add(createStatCard("Accredited Providers", verifiedEnterprisesLabel, "Local boats, guides & lodges"));
        statsGrid.add(createStatCard("Eco Conservation Fees", ecoFeeLabel, "Retained for barangay clean-up"));
        statsGrid.add(createStatCard("Compliance Rate", complianceRateLabel, "Safety & environmental rating"));

        // Table & Details Split
        JPanel contentSplit = new JPanel(new GridLayout(1, 2, 15, 15));
        contentSplit.setOpaque(false);

        // Left: Recent Arrival Logs Table
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel tableTitle = new JLabel("Jurisdiction Tourist Arrival & Check-In Logs");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        String[] cols = {"Log ID", "Date", "Destination", "Local", "Domestic", "Foreign", "Fee Collected"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        arrivalLogsTable = new JTable(tableModel);
        UITheme.styleTable(arrivalLogsTable);

        JScrollPane logScroll = new JScrollPane(arrivalLogsTable);
        logScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(logScroll, BorderLayout.CENTER);

        // Right: Detailed Action Assessment
        JPanel reportCard = UITheme.createCardPanel();
        reportCard.setLayout(new BorderLayout(10, 10));

        JLabel breakdownTitle = new JLabel("Barangay Community Tourism Assessment & Action Items");
        breakdownTitle.setFont(UITheme.FONT_SUBTITLE);
        breakdownTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(UITheme.FONT_BODY);
        summaryArea.setBackground(Color.WHITE);
        summaryArea.setText(
                "• Livelihood Impact: 38 local households directly earning from motorized banca transport and local catering.\n\n" +
                "• Coastal & Trail Clean-up: Regular coastal cleanup conducted every Saturday with 100% stakeholder participation.\n\n" +
                "• Waste Management Compliance: Zero-single-use plastics advisory posted at embarkation points.\n\n" +
                "• Safety Readiness: All accredited boat operators equipped with approved life jackets and first-aid kits.\n\n" +
                "• Environmental Fee Collection: Shared revenue remitted to Municipal Treasurer for marine sanctuary maintenance.\n\n" +
                "• Next Barangay Tourism Council Meeting: Scheduled on 15th of current month at Barangay Hall."
        );
        summaryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        reportCard.add(breakdownTitle, BorderLayout.NORTH);
        reportCard.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        contentSplit.add(tableCard);
        contentSplit.add(reportCard);

        body.add(statsGrid, BorderLayout.NORTH);
        body.add(contentSplit, BorderLayout.CENTER);
        return body;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String note) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_SMALL);
        t.setForeground(UITheme.COLOR_TEXT_SECONDARY);

        JLabel n = new JLabel(note);
        n.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        n.setForeground(UITheme.COLOR_TEXT_MUTED);

        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(n);
        return card;
    }

    public void loadStats() {
        Map<String, Object> stats = reportService.getOverviewKPIs();
        DecimalFormat currencyFmt = new DecimalFormat("#,##0.00");
        if (stats != null && !stats.isEmpty()) {
            if (stats.containsKey("totalVisitorFootfall")) {
                totalArrivalsLabel.setText(String.valueOf(stats.get("totalVisitorFootfall")));
            } else if (stats.containsKey("totalTourists")) {
                totalArrivalsLabel.setText(String.valueOf(stats.get("totalTourists")));
            }

            if (stats.containsKey("verifiedStakeholders")) {
                verifiedEnterprisesLabel.setText(String.valueOf(stats.get("verifiedStakeholders")));
            }

            double rev = 0.0;
            if (stats.containsKey("totalBookingRevenue")) {
                Object obj = stats.get("totalBookingRevenue");
                if (obj instanceof Number) rev = ((Number) obj).doubleValue();
            } else if (stats.containsKey("totalRevenue")) {
                Object obj = stats.get("totalRevenue");
                if (obj instanceof Number) rev = ((Number) obj).doubleValue();
            }
            ecoFeeLabel.setText("₱" + currencyFmt.format(rev));
        }
    }

    private void loadArrivalLogs() {
        tableModel.setRowCount(0);
        List<ArrivalRecord> logs = reportService.getArrivalLogs(null, null, null);
        for (ArrivalRecord ar : logs) {
            tableModel.addRow(new Object[]{
                    "#" + ar.getLogId(),
                    ar.getLogDate() != null ? ar.getLogDate().toString() : "N/A",
                    ar.getDestinationName() != null ? ar.getDestinationName() : "Site #" + ar.getDestinationId(),
                    ar.getLocalTouristsCount(),
                    ar.getDomesticTouristsCount(),
                    ar.getForeignTouristsCount(),
                    "₱" + (ar.getTotalRevenue() != null ? ar.getTotalRevenue().toString() : "0.00")
            });
        }
    }

    private void exportOfficialSummary() {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("========================================================================\n");
        sb.append("         LAKBAY LOKAL: BARANGAY TOURISM DESK OFFICIAL SUMMARY          \n");
        sb.append("       Province of Biliran, Eastern Visayas - Republic of the Philippines\n");
        sb.append("========================================================================\n\n");
        sb.append("Generated On: ").append(timestamp).append("\n");
        sb.append("Jurisdiction: Barangay Tourism Desk\n\n");
        sb.append("--- KEY TOURISM INDICATORS ---\n");
        sb.append("Total Visitor Footfall Logged: ").append(totalArrivalsLabel.getText()).append("\n");
        sb.append("Accredited Enterprises / Guides: ").append(verifiedEnterprisesLabel.getText()).append("\n");
        sb.append("Eco Conservation & Entrance Revenue: ").append(ecoFeeLabel.getText()).append("\n");
        sb.append("Safety & Environmental Compliance: ").append(complianceRateLabel.getText()).append("\n\n");
        sb.append("--- COMMUNITY LIVELIHOOD & CONSERVATION HIGHLIGHTS ---\n");
        sb.append("1. Active Marine Sanctuaries: Marine protected areas regularly patrolled by Bantay Dagat.\n");
        sb.append("2. Community Enterprises: Boat operators, local homestays, and tour guides verified for safety.\n");
        sb.append("3. Solid Waste Ordinance: Strict compliance with provincial zero single-use plastics mandate.\n\n");
        sb.append("--- RECENT ARRIVAL LOGS (TOTAL ENTRIES: ").append(tableModel.getRowCount()).append(") ---\n");
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append(String.format("Log %-6s | %-12s | %-24s | Local: %-4s | Dom: %-4s | For: %-4s | Rev: %s\n",
                    tableModel.getValueAt(i, 0),
                    tableModel.getValueAt(i, 1),
                    tableModel.getValueAt(i, 2),
                    tableModel.getValueAt(i, 3),
                    tableModel.getValueAt(i, 4),
                    tableModel.getValueAt(i, 5),
                    tableModel.getValueAt(i, 6)));
        }
        sb.append("\n========================================================================\n");
        sb.append("Certified Correct by: Barangay Tourism Desk Officer\n");
        sb.append("Endorsed to: Municipal Tourism Office (LGU) & Provincial Tourism Office\n");

        ExportUtil.exportTextReport(sb.toString(), "Barangay_Tourism_Summary");
    }
}
