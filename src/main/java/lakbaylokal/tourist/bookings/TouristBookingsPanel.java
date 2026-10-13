package lakbaylokal.tourist.bookings;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.Booking;
import lakbaylokal.model.BookingItem;
import lakbaylokal.model.TouristProfile;
import lakbaylokal.model.User;
import lakbaylokal.service.BookingService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.tourist.TouristDashboard;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TouristBookingsPanel extends JPanel {

    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> cmbStatus;
    private final JTextField txtSearch;
    private final TouristDashboard parentDashboard;

    private List<Booking> currentBookings = new ArrayList<>();

    public TouristBookingsPanel() {
        this(null);
    }

    public TouristBookingsPanel(TouristDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top Filter & Action Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.setOpaque(false);

        filters.add(new JLabel("🔍 Ref / Name:"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.addActionListener(e -> applyFilter());
        filters.add(txtSearch);

        filters.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"ALL", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"});
        cmbStatus.setFont(UITheme.FONT_BODY);
        cmbStatus.addActionListener(e -> applyFilter());
        filters.add(cmbStatus);

        JButton btnFilter = UITheme.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> applyFilter());
        filters.add(btnFilter);

        JButton btnReset = UITheme.createSecondaryButton("Refresh");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            loadData();
        });
        filters.add(btnReset);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton btnVoucher = UITheme.createPrimaryButton("📄 View Voucher");
        btnVoucher.addActionListener(e -> showVoucherDetails());
        actions.add(btnVoucher);

        JButton btnCancel = UITheme.createDangerButton("✕ Cancel Booking");
        btnCancel.addActionListener(e -> cancelSelectedBooking());
        actions.add(btnCancel);

        JButton btnExport = UITheme.createSecondaryButton("📥 Export Voucher");
        btnExport.addActionListener(e -> exportSelectedVoucher());
        actions.add(btnExport);

        topBar.add(filters, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Data Table
        String[] columns = {"ID", "Reference Code", "Package / Circuit", "Travel Dates", "Travelers", "Total Amount", "Status", "Payment"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = UITheme.createStyledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showVoucherDetails();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            TouristProfile tp = bookingService.getTouristProfileByUserId(user.getUserId());
            int touristId = (tp != null) ? tp.getProfileId() : 1;
            currentBookings = bookingService.getBookingsByTourist(touristId);
            if (currentBookings.isEmpty()) {
                // If user has no bookings, check user id directly
                currentBookings = bookingService.getBookingsByTourist(user.getUserId());
            }
            if (currentBookings.isEmpty()) {
                currentBookings = bookingService.getAllBookings();
            }
        } else {
            currentBookings = bookingService.getAllBookings();
        }

        applyFilter();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim().toLowerCase();
        String statusFilter = (String) cmbStatus.getSelectedItem();

        for (Booking b : currentBookings) {
            String ref = (b.getBookingReference() != null ? b.getBookingReference() : "").toLowerCase();
            String pkg = (b.getPackageName() != null ? b.getPackageName() : "Custom Travel Circuit").toLowerCase();

            if (!search.isEmpty() && !ref.contains(search) && !pkg.contains(search)) {
                continue;
            }

            if (statusFilter != null && !statusFilter.equals("ALL") && !statusFilter.equalsIgnoreCase(b.getStatus())) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getBookingReference(),
                    b.getPackageName() != null ? b.getPackageName() : "Custom Travel Circuit",
                    (b.getTravelDateStart() != null ? b.getTravelDateStart() : "") + " to " + (b.getTravelDateEnd() != null ? b.getTravelDateEnd() : ""),
                    b.getNumberOfPax() + " pax",
                    "₱" + b.getTotalAmount(),
                    b.getStatus() != null ? b.getStatus() : "PENDING",
                    b.getPaymentStatus() != null ? b.getPaymentStatus() : "UNPAID"
            });
        }
    }

    private Booking getSelectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        for (Booking b : currentBookings) {
            if (b.getBookingId() == id) return b;
        }
        return bookingService.getBookingById(id);
    }

    private void showVoucherDetails() {
        Booking b = getSelectedBooking();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view voucher.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Official Booking Voucher — " + b.getBookingReference(), true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea voucherArea = new JTextArea();
        voucherArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        voucherArea.setEditable(false);
        voucherArea.setBackground(Color.WHITE);
        voucherArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        voucherArea.setText(generateVoucherText(b));

        JScrollPane scroll = new JScrollPane(voucherArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton exportBtn = UITheme.createSuccessButton("📥 Save / Print Voucher");
        exportBtn.addActionListener(e -> {
            ExportUtil.exportTextReport(dialog, voucherArea.getText(), "Voucher_" + b.getBookingReference(), "Lakbay Lokal Travel Voucher");
        });

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        btns.add(exportBtn);
        btns.add(closeBtn);

        root.add(scroll, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void cancelSelectedBooking() {
        Booking b = getSelectedBooking();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "This booking has already been cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "Completed bookings cannot be cancelled.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel reservation " + b.getBookingReference() + "?\nThis action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = bookingService.updateBookingStatus(b.getBookingId(), "CANCELLED");
            b.setStatus("CANCELLED");
            loadData();
            JOptionPane.showMessageDialog(this, "Booking " + b.getBookingReference() + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportSelectedVoucher() {
        Booking b = getSelectedBooking();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking to export voucher.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ExportUtil.exportTextReport(this, generateVoucherText(b), "Voucher_" + b.getBookingReference(), "Lakbay Lokal Travel Voucher");
    }

    private String generateVoucherText(Booking b) {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                LAKBAY LOKAL — OFFICIAL TRAVEL VOUCHER               \n");
        sb.append("           Community-Based Tourism Information System • Biliran        \n");
        sb.append("======================================================================\n\n");
        sb.append("BOOKING REFERENCE:  ").append(b.getBookingReference()).append("\n");
        sb.append("RESERVATION STATUS: ").append(b.getStatus()).append("\n");
        sb.append("PAYMENT STATUS:     ").append(b.getPaymentStatus()).append("\n");
        sb.append("BOOKING DATE:       ").append(b.getBookingDate() != null ? b.getBookingDate() : "2026-03-01").append("\n\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append("GUEST DETAILS:\n");
        sb.append("• Lead Traveler:    ").append(b.getTouristName() != null ? b.getTouristName() : "Ana Sophia Cortez").append("\n");
        sb.append("• Contact Email:    ").append(b.getTouristEmail() != null ? b.getTouristEmail() : "ana.tourist@gmail.com").append("\n");
        sb.append("• Contact Mobile:   ").append(b.getTouristPhone() != null ? b.getTouristPhone() : "+63 915 777 3333").append("\n");
        sb.append("• Total Pax:        ").append(b.getNumberOfPax()).append(" traveler(s)\n\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append("ITINERARY & RESERVED SERVICES:\n");
        sb.append("• Package/Circuit:  ").append(b.getPackageName() != null ? b.getPackageName() : "Custom Community Experience").append("\n");
        sb.append("• Travel Dates:     ").append(b.getTravelDateStart()).append(" to ").append(b.getTravelDateEnd()).append("\n");

        if (b.getItems() != null && !b.getItems().isEmpty()) {
            sb.append("\nINCLUDED COMMUNITY SERVICES:\n");
            for (BookingItem item : b.getItems()) {
                sb.append("  - ").append(item.getServiceName() != null ? item.getServiceName() : "Community Service")
                  .append(" (").append(item.getBusinessName() != null ? item.getBusinessName() : "Local Stakeholder").append(")")
                  .append(": ₱").append(item.getSubtotal()).append("\n");
            }
        }

        if (b.getCustomNotes() != null && !b.getCustomNotes().isBlank()) {
            sb.append("\n• Special Notes:    ").append(b.getCustomNotes()).append("\n");
        }

        sb.append("\n----------------------------------------------------------------------\n");
        sb.append("TOTAL AMOUNT:       ₱").append(String.format("%,.2f", b.getTotalAmount().doubleValue())).append("\n");
        sb.append("======================================================================\n");
        sb.append("IMPORTANT REMINDERS:\n");
        sb.append("1. Present this digital or printed voucher at embarkation points in Biliran.\n");
        sb.append("2. Coordinate directly with your accredited local boatmen and guides.\n");
        sb.append("3. Observe 'Leave No Trace' principles and environmental regulations.\n");
        sb.append("Hotline: Biliran Provincial Tourism Office • tourism@biliran.gov.ph\n");

        return sb.toString();
    }
}
