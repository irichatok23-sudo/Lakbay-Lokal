package lakbaylokal.stakeholder.bookings;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.Booking;
import lakbaylokal.model.BookingItem;
import lakbaylokal.model.StakeholderProfile;
import lakbaylokal.model.User;
import lakbaylokal.service.BookingService;
import lakbaylokal.service.StakeholderService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StakeholderBookingsPanel extends JPanel {
    private final BookingService bookingService = new BookingService();
    private final StakeholderService stakeholderService = new StakeholderService();

    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbStatus;

    private List<Booking> currentBookings = new ArrayList<>();
    private int currentStakeholderProfileId = 3;

    public StakeholderBookingsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top Filter Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.setOpaque(false);

        filters.add(new JLabel("🔍 Search:"));
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
            loadBookings();
        });
        filters.add(btnReset);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton btnVoucher = UITheme.createPrimaryButton("📄 View Breakdown");
        btnVoucher.addActionListener(e -> viewSelectedBreakdown());
        actions.add(btnVoucher);

        JButton btnExport = UITheme.createSecondaryButton("📥 Export");
        btnExport.addActionListener(e -> ExportUtil.exportTableToCSV(this, bookingsTable, "Stakeholder_Bookings", "Stakeholder Client Reservations"));
        actions.add(btnExport);

        topBar.add(filters, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Data Table
        String[] cols = {"ID", "Booking Ref", "Guest / Tourist", "Package / Service Circuit", "Travel Dates", "Pax", "Total (PHP)", "Status", "Payment"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingsTable = UITheme.createStyledTable(tableModel);
        bookingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedBreakdown();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        // Bottom Operations Bar
        JPanel operations = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        operations.setOpaque(false);

        JButton confirmBtn = UITheme.createSuccessButton("✓ Accept & Confirm Reservation");
        confirmBtn.addActionListener(e -> updateStatus("CONFIRMED"));

        JButton completeBtn = UITheme.createPrimaryButton("✓ Complete Service Fulfillment");
        completeBtn.addActionListener(e -> updateStatus("COMPLETED"));

        JButton rejectBtn = UITheme.createDangerButton("✕ Decline / Cancel Slot");
        rejectBtn.addActionListener(e -> updateStatus("CANCELLED"));

        operations.add(confirmBtn);
        operations.add(completeBtn);
        operations.add(rejectBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(operations, BorderLayout.SOUTH);

        loadBookings();
    }

    public void loadBookings() {
        resolveCurrentStakeholderProfileId();
        currentBookings = bookingService.getBookingsByStakeholder(currentStakeholderProfileId);
        if (currentBookings.isEmpty()) {
            currentBookings = bookingService.getAllBookings();
        }
        applyFilter();
    }

    private void resolveCurrentStakeholderProfileId() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            StakeholderProfile sp = stakeholderService.getStakeholderByUserId(user.getUserId());
            if (sp != null) {
                currentStakeholderProfileId = sp.getProfileId();
                return;
            }
        }
        currentStakeholderProfileId = 3;
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim().toLowerCase();
        String status = (String) cmbStatus.getSelectedItem();

        for (Booking b : currentBookings) {
            String ref = (b.getBookingReference() != null ? b.getBookingReference() : "").toLowerCase();
            String guest = (b.getTouristName() != null ? b.getTouristName() : "").toLowerCase();

            if (!search.isEmpty() && !ref.contains(search) && !guest.contains(search)) {
                continue;
            }

            if (status != null && !status.equals("ALL") && !status.equalsIgnoreCase(b.getStatus())) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getBookingReference(),
                    b.getTouristName() != null ? b.getTouristName() : "Tourist #" + b.getTouristId(),
                    b.getPackageName() != null ? b.getPackageName() : "Custom Community Service",
                    (b.getTravelDateStart() != null ? b.getTravelDateStart() : "") + " to " + (b.getTravelDateEnd() != null ? b.getTravelDateEnd() : ""),
                    b.getNumberOfPax() + " pax",
                    "₱" + b.getTotalAmount(),
                    b.getStatus() != null ? b.getStatus() : "PENDING",
                    b.getPaymentStatus() != null ? b.getPaymentStatus() : "UNPAID"
            });
        }
    }

    private Booking getSelectedBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        for (Booking b : currentBookings) {
            if (b.getBookingId() == id) return b;
        }
        return bookingService.getBookingById(id);
    }

    private void updateStatus(String newStatus) {
        Booking b = getSelectedBooking();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Please select a client reservation from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Change status of reservation " + b.getBookingReference() + " to " + newStatus + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = bookingService.updateBookingStatus(b.getBookingId(), newStatus);
            b.setStatus(newStatus);
            loadBookings();
            JOptionPane.showMessageDialog(this, "Booking status successfully updated to: " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewSelectedBreakdown() {
        Booking b = getSelectedBooking();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view details.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reservation Breakdown — " + b.getBookingReference(), true);
        dialog.setSize(540, 480);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = UITheme.createCardPanel("Booking Voucher & Guest Information");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(new JLabel("<html><b>Reference Number:</b> " + b.getBookingReference() + "</html>"));
        card.add(Box.createVerticalStrut(6));
        card.add(new JLabel("<html><b>Guest Name:</b> " + (b.getTouristName() != null ? b.getTouristName() : "Ana Sophia Cortez") + "</html>"));
        card.add(Box.createVerticalStrut(6));
        card.add(new JLabel("<html><b>Contact:</b> " + (b.getTouristEmail() != null ? b.getTouristEmail() : "ana.tourist@gmail.com") + " (" + (b.getTouristPhone() != null ? b.getTouristPhone() : "+63 915 777 3333") + ")</html>"));
        card.add(Box.createVerticalStrut(6));
        card.add(new JLabel("<html><b>Travel Dates:</b> " + b.getTravelDateStart() + " to " + b.getTravelDateEnd() + " (" + b.getNumberOfPax() + " travelers)</html>"));
        card.add(Box.createVerticalStrut(6));
        card.add(new JLabel("<html><b>Status:</b> " + b.getStatus() + " | <b>Payment:</b> " + b.getPaymentStatus() + "</html>"));
        card.add(Box.createVerticalStrut(6));
        card.add(new JLabel("<html><b>Total Amount:</b> ₱" + b.getTotalAmount() + "</html>"));
        card.add(Box.createVerticalStrut(12));

        if (b.getCustomNotes() != null && !b.getCustomNotes().isBlank()) {
            card.add(new JLabel("<html><b>Special Notes:</b> " + b.getCustomNotes() + "</html>"));
            card.add(Box.createVerticalStrut(12));
        }

        if (b.getItems() != null && !b.getItems().isEmpty()) {
            JLabel itemsHeader = new JLabel("Service Items Reserved:");
            itemsHeader.setFont(UITheme.FONT_BOLD);
            card.add(itemsHeader);
            card.add(Box.createVerticalStrut(4));

            DefaultTableModel itemModel = new DefaultTableModel(new String[]{"Service", "Unit Rate", "Qty", "Subtotal"}, 0);
            for (BookingItem item : b.getItems()) {
                itemModel.addRow(new Object[]{
                        item.getServiceName() != null ? item.getServiceName() : "Community Service",
                        "₱" + item.getUnitPrice(),
                        item.getQuantity(),
                        "₱" + item.getSubtotal()
                });
            }
            JTable itemTable = UITheme.createStyledTable(itemModel);
            card.add(new JScrollPane(itemTable));
        }

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton confirmBtn = UITheme.createSuccessButton("Confirm This Booking");
        confirmBtn.addActionListener(e -> {
            dialog.dispose();
            updateStatus("CONFIRMED");
        });

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        btnBar.add(confirmBtn);
        btnBar.add(closeBtn);

        root.add(card, BorderLayout.CENTER);
        root.add(btnBar, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
