package lakbaylokal.tourist.destinations;

import lakbaylokal.model.Destination;
import lakbaylokal.service.DestinationService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.tourist.TouristDashboard;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TouristDestinationsPanel extends JPanel {

    private final DestinationService destinationService = new DestinationService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> cmbCategory;
    private final JComboBox<String> cmbMunicipality;
    private final JTextField txtSearch;
    private final TouristDashboard parentDashboard;

    private List<Destination> currentList = new ArrayList<>();

    public TouristDestinationsPanel() {
        this(null);
    }

    public TouristDestinationsPanel(TouristDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Data Table
        String[] columns = {"ID", "Destination Name", "Category", "Barangay", "Municipality", "Entrance / Eco Fee", "Operating Hours", "Rating", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = UITheme.createStyledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelectedDetails();
                }
            }
        });

        // Filter & Search Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.setOpaque(false);

        filters.add(new JLabel("🔍 Search:"));
        txtSearch = new JTextField(14);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.addActionListener(e -> applyFilter());
        filters.add(txtSearch);

        filters.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"ALL", "WATERFALL", "ISLAND_BEACH", "HOT_SPRING", "CULTURAL_HERITAGE", "CAVE_MOUNTAIN"});
        cmbCategory.setFont(UITheme.FONT_BODY);
        cmbCategory.addActionListener(e -> applyFilter());
        filters.add(cmbCategory);

        filters.add(new JLabel("Municipality:"));
        cmbMunicipality = new JComboBox<>(new String[]{"ALL", "Naval", "Almeria", "Caibiran", "Maripipi", "Kawayan", "Biliran", "Cabucgayan", "Culaba"});
        cmbMunicipality.setFont(UITheme.FONT_BODY);
        cmbMunicipality.addActionListener(e -> applyFilter());
        filters.add(cmbMunicipality);

        JButton btnApply = UITheme.createPrimaryButton("Filter");
        btnApply.addActionListener(e -> applyFilter());
        filters.add(btnApply);

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            cmbMunicipality.setSelectedIndex(0);
            loadData();
        });
        filters.add(btnReset);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton btnDetails = UITheme.createSuccessButton("ℹ View Spot Details");
        btnDetails.addActionListener(e -> showSelectedDetails());
        actions.add(btnDetails);

        JButton btnExport = UITheme.createSecondaryButton("📥 Export Catalog");
        btnExport.addActionListener(e -> ExportUtil.exportTableToCSV(this, table, "Biliran_Destinations", "Biliran Province Tourist Destinations Catalog"));
        actions.add(btnExport);

        topBar.add(filters, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        currentList = destinationService.getDestinations(null, null, "ACTIVE");
        applyFilter();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim().toLowerCase();
        String cat = (String) cmbCategory.getSelectedItem();
        String mun = (String) cmbMunicipality.getSelectedItem();

        for (Destination d : currentList) {
            // Category match
            if (cat != null && !cat.equals("ALL") && !cat.equalsIgnoreCase(d.getCategory())) {
                continue;
            }
            // Municipality match
            if (mun != null && !mun.equals("ALL")) {
                String dMun = d.getMunicipalityName() != null ? d.getMunicipalityName() : d.getMunicipality();
                if (dMun == null || !dMun.equalsIgnoreCase(mun)) {
                    continue;
                }
            }
            // Search text match
            if (!search.isEmpty()) {
                String name = d.getName() != null ? d.getName().toLowerCase() : "";
                String desc = d.getDescription() != null ? d.getDescription().toLowerCase() : "";
                String dMun = d.getMunicipalityName() != null ? d.getMunicipalityName().toLowerCase() : "";
                if (!name.contains(search) && !desc.contains(search) && !dMun.contains(search)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    d.getDestinationId(),
                    d.getName(),
                    d.getCategory(),
                    d.getBarangayName() != null ? d.getBarangayName() : (d.getBarangay() != null ? d.getBarangay() : "Center"),
                    d.getMunicipalityName() != null ? d.getMunicipalityName() : (d.getMunicipality() != null ? d.getMunicipality() : "Biliran"),
                    "₱" + d.getEntranceFee(),
                    d.getOperatingHours() != null ? d.getOperatingHours() : "6:00 AM - 5:00 PM",
                    (d.getAverageRating() > 0 ? d.getAverageRating() : 4.8) + " ★",
                    d.getStatus() != null ? d.getStatus() : "ACTIVE"
            });
        }
    }

    private void showSelectedDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a destination from the table to view full details.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int destId = (int) tableModel.getValueAt(row, 0);
        Destination dest = null;
        for (Destination d : currentList) {
            if (d.getDestinationId() == destId) {
                dest = d;
                break;
            }
        }
        if (dest == null) dest = destinationService.getDestinationById(destId);
        if (dest == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Destination Spot Details — " + dest.getName(), true);
        dialog.setSize(600, 520);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(dest.getName());
        nameLbl.setFont(UITheme.FONT_TITLE);
        nameLbl.setForeground(UITheme.PRIMARY_DARK);

        String mun = dest.getMunicipalityName() != null ? dest.getMunicipalityName() : dest.getMunicipality();
        String brgy = dest.getBarangayName() != null ? dest.getBarangayName() : dest.getBarangay();
        JLabel locLbl = new JLabel("📍 " + (brgy != null ? brgy + ", " : "") + mun + ", Biliran Province");
        locLbl.setFont(UITheme.FONT_BOLD);
        locLbl.setForeground(UITheme.TEAL);

        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(locLbl);
        card.add(Box.createVerticalStrut(15));

        // Details Grid
        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 8, 8));
        infoGrid.setOpaque(false);
        infoGrid.add(new JLabel("Category: " + dest.getCategory()));
        infoGrid.add(new JLabel("Entrance / Eco Fee: ₱" + dest.getEntranceFee()));
        infoGrid.add(new JLabel("Operating Hours: " + (dest.getOperatingHours() != null ? dest.getOperatingHours() : "6:00 AM - 5:00 PM")));
        infoGrid.add(new JLabel("Rating: " + (dest.getAverageRating() > 0 ? dest.getAverageRating() : 4.8) + " ★"));
        infoGrid.add(new JLabel("Coordinates: " + (dest.getLocationCoordinates() != null ? dest.getLocationCoordinates() : "11.6025, 124.2628")));
        infoGrid.add(new JLabel("Contact: " + (dest.getContactInfo() != null ? dest.getContactInfo() : "tourism@biliran.gov.ph")));
        infoGrid.add(new JLabel("Status: " + dest.getStatus()));
        infoGrid.add(new JLabel("DOT Standard: Certified Safe"));

        card.add(infoGrid);
        card.add(Box.createVerticalStrut(15));

        JLabel descTitle = new JLabel("Attraction Description & Travel Overview:");
        descTitle.setFont(UITheme.FONT_BOLD);
        card.add(descTitle);
        card.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea(dest.getDescription() != null ? dest.getDescription() : "Beautiful local Biliran eco-destination.");
        descArea.setFont(UITheme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.add(new JScrollPane(descArea));

        // Bottom Action Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton btnPlan = UITheme.createPrimaryButton("Plan Custom Itinerary with this Spot →");
        btnPlan.addActionListener(e -> {
            dialog.dispose();
            if (parentDashboard != null) {
                parentDashboard.switchToTab(3); // Switch to Itinerary Planner
            }
        });

        JButton btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());

        btnBar.add(btnPlan);
        btnBar.add(btnClose);

        root.add(card, BorderLayout.CENTER);
        root.add(btnBar, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
