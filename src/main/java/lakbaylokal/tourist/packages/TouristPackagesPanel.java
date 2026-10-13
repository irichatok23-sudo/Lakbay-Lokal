package lakbaylokal.tourist.packages;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.Booking;
import lakbaylokal.model.TourismPackage;
import lakbaylokal.model.TouristProfile;
import lakbaylokal.model.User;
import lakbaylokal.service.BookingService;
import lakbaylokal.service.PackageService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.tourist.TouristDashboard;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TouristPackagesPanel extends JPanel {

    private final PackageService packageService = new PackageService();
    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtSearch;
    private final TouristDashboard parentDashboard;

    private List<TourismPackage> currentPackages = new ArrayList<>();

    public TouristPackagesPanel() {
        this(null);
    }

    public TouristPackagesPanel(TouristDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Data Table
        String[] columns = {"ID", "Package Title", "Duration", "Min - Max Pax", "Rate / Person", "Key Inclusions", "Rating", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = UITheme.createStyledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bookSelectedPackage();
                }
            }
        });

        // Top Filter & Action Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.setOpaque(false);

        filters.add(new JLabel("🔍 Search Packages:"));
        txtSearch = new JTextField(16);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.addActionListener(e -> applyFilter());
        filters.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> applyFilter());
        filters.add(btnSearch);

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });
        filters.add(btnReset);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton btnBook = UITheme.createSuccessButton("⚡ Book Selected Package");
        btnBook.addActionListener(e -> bookSelectedPackage());
        actions.add(btnBook);

        JButton btnDetails = UITheme.createSecondaryButton("ℹ Package Details");
        btnDetails.addActionListener(e -> showPackageDetails());
        actions.add(btnDetails);

        JButton btnExport = UITheme.createSecondaryButton("📥 Export");
        btnExport.addActionListener(e -> ExportUtil.exportTableToCSV(this, table, "Biliran_Tourism_Packages", "Biliran Province Community Packages"));
        actions.add(btnExport);

        topBar.add(filters, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        currentPackages = packageService.getAllPackages();
        applyFilter();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim().toLowerCase();

        for (TourismPackage p : currentPackages) {
            String name = (p.getName() != null ? p.getName() : (p.getPackageName() != null ? p.getPackageName() : "")).toLowerCase();
            String incl = (p.getInclusions() != null ? p.getInclusions() : "").toLowerCase();
            String desc = (p.getDescription() != null ? p.getDescription() : "").toLowerCase();

            if (!search.isEmpty() && !name.contains(search) && !incl.contains(search) && !desc.contains(search)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    p.getPackageId(),
                    p.getName() != null ? p.getName() : p.getPackageName(),
                    p.getDurationDays() + " Day(s)",
                    p.getMinPax() + " - " + p.getMaxPax() + " pax",
                    "₱" + (p.getPricePerPax() != null ? p.getPricePerPax() : p.getPrice()),
                    p.getInclusions() != null ? p.getInclusions() : "Local guides, transfers, meals",
                    (p.getAverageRating() > 0 ? p.getAverageRating() : 4.9) + " ★",
                    p.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }

    private TourismPackage getSelectedPackage() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        for (TourismPackage p : currentPackages) {
            if (p.getPackageId() == id) return p;
        }
        return packageService.getPackageById(id);
    }

    private void showPackageDetails() {
        TourismPackage pkg = getSelectedPackage();
        if (pkg == null) {
            JOptionPane.showMessageDialog(this, "Please select a package from the table to view details.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String title = pkg.getName() != null ? pkg.getName() : pkg.getPackageName();
        BigDecimal price = pkg.getPricePerPax() != null ? pkg.getPricePerPax() : pkg.getPrice();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Package Details — " + title, true);
        dialog.setSize(560, 460);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);

        JLabel priceLbl = new JLabel("₱" + price + " per person • Duration: " + pkg.getDurationDays() + " Day(s)");
        priceLbl.setFont(UITheme.FONT_HEADER);
        priceLbl.setForeground(UITheme.TEAL);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(priceLbl);
        card.add(Box.createVerticalStrut(15));

        card.add(new JLabel("Participant Capacity: " + pkg.getMinPax() + " to " + pkg.getMaxPax() + " travelers"));
        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("Inclusions: " + (pkg.getInclusions() != null ? pkg.getInclusions() : "Guides, transfers, fees")));
        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("Exclusions: " + (pkg.getExclusions() != null ? pkg.getExclusions() : "Personal expenses")));
        card.add(Box.createVerticalStrut(15));

        JLabel descLbl = new JLabel("Package Description & Highlights:");
        descLbl.setFont(UITheme.FONT_BOLD);
        card.add(descLbl);
        card.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea(pkg.getDescription() != null ? pkg.getDescription() : "Authentic community tourism experience in Biliran Province.");
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

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton btnBook = UITheme.createSuccessButton("Book This Package Now →");
        btnBook.addActionListener(e -> {
            dialog.dispose();
            openBookingDialog(pkg);
        });

        JButton btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());

        btns.add(btnBook);
        btns.add(btnClose);

        root.add(card, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void bookSelectedPackage() {
        TourismPackage pkg = getSelectedPackage();
        if (pkg == null) {
            JOptionPane.showMessageDialog(this, "Please select a package from the table to reserve.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        openBookingDialog(pkg);
    }

    private void openBookingDialog(TourismPackage pkg) {
        String title = pkg.getName() != null ? pkg.getName() : pkg.getPackageName();
        BigDecimal pricePerPax = pkg.getPricePerPax() != null ? pkg.getPricePerPax() : pkg.getPrice();
        if (pricePerPax == null) pricePerPax = new BigDecimal("1500.00");

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reserve Community Package — " + title, true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridLayout(6, 2, 10, 14));

        JTextField txtStartDate = new JTextField(LocalDate.now().plusDays(2).toString());
        JTextField txtEndDate = new JTextField(LocalDate.now().plusDays(2 + pkg.getDurationDays()).toString());
        JSpinner spinPax = new JSpinner(new SpinnerNumberModel(2, 1, 25, 1));
        JTextField txtNotes = new JTextField("Requesting local sea tour guide");
        JLabel lblTotal = new JLabel("₱" + (pricePerPax.doubleValue() * 2));
        lblTotal.setFont(UITheme.FONT_TITLE);
        lblTotal.setForeground(UITheme.PRIMARY);

        final BigDecimal rate = pricePerPax;
        spinPax.addChangeListener(e -> {
            int p = (int) spinPax.getValue();
            double total = rate.doubleValue() * p;
            lblTotal.setText(String.format("₱%,.2f", total));
        });

        formCard.add(new JLabel("Package:"));
        formCard.add(new JLabel("<html><b>" + title + "</b></html>"));

        formCard.add(new JLabel("Travel Start (YYYY-MM-DD):"));
        formCard.add(txtStartDate);

        formCard.add(new JLabel("Travel End (YYYY-MM-DD):"));
        formCard.add(txtEndDate);

        formCard.add(new JLabel("Number of Travelers (Pax):"));
        formCard.add(spinPax);

        formCard.add(new JLabel("Special Requests / Notes:"));
        formCard.add(txtNotes);

        formCard.add(new JLabel("Total Amount:"));
        formCard.add(lblTotal);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton confirmBtn = UITheme.createSuccessButton("Confirm Reservation");
        confirmBtn.addActionListener(e -> {
            int pax = (int) spinPax.getValue();
            double total = rate.doubleValue() * pax;

            User user = SessionManager.getCurrentUser();
            TouristProfile tp = (user != null) ? bookingService.getTouristProfileByUserId(user.getUserId()) : null;
            int touristProfileId = (tp != null) ? tp.getProfileId() : 1;

            Booking b = new Booking();
            b.setTouristId(touristProfileId);
            b.setPackageId(pkg.getPackageId());
            try {
                b.setTravelDateStart(Date.valueOf(txtStartDate.getText().trim()));
                b.setTravelDateEnd(Date.valueOf(txtEndDate.getText().trim()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            b.setNumberOfPax(pax);
            b.setTotalAmount(BigDecimal.valueOf(total));
            b.setStatus("PENDING");
            b.setPaymentStatus("UNPAID");
            b.setCustomNotes(txtNotes.getText().trim());

            boolean ok = bookingService.createBooking(b);
            if (ok) {
                dialog.dispose();
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "🎉 Reservation Successfully Placed!\n\n" +
                                "Booking Reference: " + b.getBookingReference() + "\n" +
                                "Package: " + title + "\n" +
                                "Total: " + String.format("₱%,.2f", total) + "\n\n" +
                                "Would you like to view your booking in 'My Bookings' tab?",
                        "Booking Confirmed",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"View My Bookings", "Stay Here"},
                        "View My Bookings"
                );

                if (choice == 0 && parentDashboard != null) {
                    parentDashboard.switchToTab(2); // My Bookings tab
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Unable to save reservation. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(confirmBtn);

        panel.add(formCard, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
