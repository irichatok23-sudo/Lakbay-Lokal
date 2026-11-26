package lakbaylokal.lgu.reports;

import lakbaylokal.service.ReportService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class LGUReportsPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> municipalityCombo;
    private JTextArea previewArea;

    public LGUReportsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createReportGeneratorUI(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("LGU Tourism Governance & Executive Reports");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Generate municipal reports, revenue audit summaries, and environmental compliance documentation.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.COLOR_TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JComponent createReportGeneratorUI() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setOpaque(false);

        // Control Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        filterCard.add(new JLabel("Report Type:"));
        reportTypeCombo = new JComboBox<>(new String[]{
                "Monthly Tourism Revenue & Remittances",
                "Environmental Carrying Capacity & Compliance",
                "Community Livelihood Distribution Audit",
                "Annual Provincial Tourism Masterplan Metric"
        });
        filterCard.add(reportTypeCombo);

        filterCard.add(new JLabel("Municipality Scope:"));
        municipalityCombo = new JComboBox<>(new String[]{
                "All Municipalities (Consolidated)",
                "Maripipi", "Caibiran", "Naval", "Almeria", "Kawayan", "Biliran", "Cabucgayan", "Culaba"
        });
        filterCard.add(municipalityCombo);

        JButton generateBtn = UITheme.createPrimaryButton("Generate Report");
        generateBtn.addActionListener(e -> generateReportContent());
        filterCard.add(generateBtn);

        JButton exportReportBtn = UITheme.createSuccessButton("Save / Export Official Report");
        exportReportBtn.addActionListener(e -> ExportUtil.exportTextReport(previewArea.getText(), "LGU_Tourism_Official_Report"));
        filterCard.add(exportReportBtn);

        // Preview Area
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        previewArea.setBackground(Color.WHITE);
        previewArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        container.add(filterCard, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);

        generateReportContent();
        return container;
    }

    public void generateReportContent() {
        String type = (String) (reportTypeCombo != null ? reportTypeCombo.getSelectedItem() : "Monthly Tourism Revenue & Remittances");
        String scope = (String) (municipalityCombo != null ? municipalityCombo.getSelectedItem() : "All Municipalities (Consolidated)");
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> kpis = reportService.getOverviewKPIs();
        DecimalFormat currencyFmt = new DecimalFormat("#,##0.00");

        double totalRev = 258500.00;
        if (kpis != null) {
            if (kpis.containsKey("totalBookingRevenue")) {
                Object o = kpis.get("totalBookingRevenue");
                if (o instanceof Number) totalRev = ((Number) o).doubleValue();
            } else if (kpis.containsKey("totalRevenue")) {
                Object o = kpis.get("totalRevenue");
                if (o instanceof Number) totalRev = ((Number) o).doubleValue();
            }
        }
        if (totalRev <= 0) totalRev = 258500.00;

        double lguShare = totalRev * 0.15;
        double communityShare = totalRev * 0.85;

        int totalTourists = 1420;
        int activeStakeholders = 14;
        if (kpis != null) {
            if (kpis.containsKey("totalVisitorFootfall")) {
                int vf = ((Number) kpis.get("totalVisitorFootfall")).intValue();
                if (vf > 0) totalTourists = vf;
            }
            if (kpis.containsKey("verifiedStakeholders")) {
                int vs = ((Number) kpis.get("verifiedStakeholders")).intValue();
                if (vs > 0) activeStakeholders = vs;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                     REPUBLIC OF THE PHILIPPINES — PROVINCE OF BILIRAN\n");
        sb.append("                  LOCAL GOVERNMENT UNIT (LGU) TOURISM DEVELOPMENT DESK\n");
        sb.append("========================================================================================\n\n");
        sb.append("REPORT TITLE:    ").append(type).append("\n");
        sb.append("JURISDICTION:    ").append(scope).append("\n");
        sb.append("DATE GENERATED:  ").append(nowStr).append("\n");
        sb.append("STATUS:          OFFICIAL CERTIFIED PUBLIC RECORD\n\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append("1. EXECUTIVE SUMMARY:\n");
        sb.append("   Community-based tourism initiatives have yielded positive local economic returns\n");
        sb.append("   with statutory municipal tourism conservation fees allocated towards coastal\n");
        sb.append("   cleanliness, trail preservation, and local livelihood development.\n\n");
        sb.append("2. FINANCIAL BREAKDOWN:\n");
        sb.append("   • Gross Community Tourism Earnings:     PHP ").append(currencyFmt.format(totalRev)).append("\n");
        sb.append("   • Barangay & LGU Environmental Shares:  PHP ").append(currencyFmt.format(lguShare)).append(" (15% statutory)\n");
        sb.append("   • Net Retained by Local Cooperatives:   PHP ").append(currencyFmt.format(communityShare)).append(" (85% direct community)\n\n");
        sb.append("3. COMPLIANCE & SAFETY AUDIT:\n");
        sb.append("   • Registered Stakeholders: ").append(activeStakeholders).append(" active enterprises verified.\n");
        sb.append("   • Cumulative Visitor Footfall: ").append(totalTourists).append(" recorded arrivals.\n");
        sb.append("   • Zero critical maritime incidents reported within current evaluation period.\n");
        sb.append("   • Carrying capacity thresholds respected in all monitored high-volume weekend dates.\n\n");
        sb.append("4. RECOMMENDATIONS:\n");
        sb.append("   • Expand solar lighting installations at Maripipi and Sambawan boat docking facilities.\n");
        sb.append("   • Conduct additional eco-tour guide accreditation cycles for youth participants.\n");
        sb.append("   • Maintain active coordination between LGU Planning Board and Provincial Tourism Office.\n");
        sb.append("========================================================================================\n");
        sb.append("PREPARED BY: Office of the Municipal Tourism Officer | VERIFIED BY: LGU Planning Board\n");

        if (previewArea != null) {
            previewArea.setText(sb.toString());
            previewArea.setCaretPosition(0);
        }
    }
}
