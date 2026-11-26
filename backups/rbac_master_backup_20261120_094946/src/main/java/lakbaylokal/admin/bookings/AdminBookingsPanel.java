package lakbaylokal.admin.bookings;

import lakbaylokal.model.Booking;
import lakbaylokal.service.BookingService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminBookingsPanel extends JPanel {
    private final BookingService bookingService = new BookingService();

    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;

    public AdminBookingsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableSection(), BorderLayout.CENTER);

        loadBookings();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("System Bookings & Financial Transactions");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Master administrative management of reservations, vouchers, payments, and cancellations.");
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
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(bookingsTable, "Admin_Master_Bookings"));

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh List");
        refreshBtn.addActionListener(e -> loadBookings());

        actionPanel.add(exportBtn);
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
        searchField.putClientProperty("JTextField.placeholderText", "Ref or tourist name...");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "ACCEPTED", "CONFIRMED", "COMPLETED", "CANCELLED"});
        filterBar.add(statusFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.addActionListener(e -> loadBookings());
        filterBar.add(filterBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadBookings();
        });
        filterBar.add(resetBtn);

        // Table
        String[] cols = {"ID", "Booking Ref", "Tourist / Client", "Package Title", "Travel Date", "Pax", "Total Amount", "Booking Status", "Payment Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingsTable = new JTable(tableModel);
        UITheme.styleTable(bookingsTable);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // Action Toolbar
        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footerActions.setOpaque(false);

        JButton viewDetailsBtn = UITheme.createSecondaryButton("View Booking Voucher");
        viewDetailsBtn.addActionListener(e -> viewBookingDetails());

        JButton confirmBtn = UITheme.createSuccessButton("Override: Confirm");
        confirmBtn.addActionListener(e -> updateStatus("CONFIRMED"));

        JButton completeBtn = UITheme.createPrimaryButton("Override: Completed");
        completeBtn.addActionListener(e -> updateStatus("COMPLETED"));

        JButton cancelBtn = UITheme.createDangerButton("Override: Cancel");
        cancelBtn.addActionListener(e -> updateStatus("CANCELLED"));

        JButton markPaidBtn = UITheme.createWarningButton("Mark Paid");
        markPaidBtn.addActionListener(e -> updatePayment("PAID"));

        footerActions.add(viewDetailsBtn);
        footerActions.add(confirmBtn);
        footerActions.add(completeBtn);
        footerActions.add(cancelBtn);
        footerActions.add(markPaidBtn);

        container.add(filterBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footerActions, BorderLayout.SOUTH);
        return container;
    }

    public void loadBookings() {
        tableModel.setRowCount(0);
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = (String) (statusFilterCombo != null ? statusFilterCombo.getSelectedItem() : "ALL");

        List<Booking> list = bookingService.getAllBookings();
        for (Booking b : list) {
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL") && !statusFilter.equalsIgnoreCase(b.getStatus())) {
                continue;
            }
            if (!kw.isEmpty()) {
                String combined = (b.getReferenceNumber() + " " + b.getTouristName() + " " + b.getPackageName()).toLowerCase();
                if (!combined.contains(kw)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getReferenceNumber(),
                    b.getTouristName() != null ? b.getTouristName() : "Tourist #" + b.getTouristId(),
                    b.getPackageName() != null ? b.getPackageName() : "Package #" + b.getPackageId(),
                    b.getTravelDate(),
                    b.getNumPax() + " pax",
                    "₱" + b.getTotalAmount(),
                    b.getStatus(),
                    b.getPaymentStatus()
            });
        }
    }

    private void viewBookingDetails() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to view.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Booking b = bookingService.getBookingById(id);
        if (b == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Booking Voucher: " + b.getReferenceNumber(), true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(8, 2, 10, 8));
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        p.add(new JLabel("Reference Number:"));
        p.add(new JLabel("<html><b>" + b.getReferenceNumber() + "</b></html>"));
        p.add(new JLabel("Tourist Client:"));
        p.add(new JLabel(b.getTouristName() != null ? b.getTouristName() : "Tourist #" + b.getTouristId()));
        p.add(new JLabel("Package Experience:"));
        p.add(new JLabel(b.getPackageName() != null ? b.getPackageName() : "Custom Package"));
        p.add(new JLabel("Travel Date:"));
        p.add(new JLabel(b.getTravelDate() != null ? b.getTravelDate().toString() : "N/A"));
        p.add(new JLabel("Participants:"));
        p.add(new JLabel(b.getNumPax() + " person(s)"));
        p.add(new JLabel("Total Transaction Amount:"));
        p.add(new JLabel("₱" + b.getTotalAmount()));
        p.add(new JLabel("Booking Status:"));
        p.add(new JLabel(b.getStatus()));
        p.add(new JLabel("Payment Status:"));
        p.add(new JLabel(b.getPaymentStatus()));

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(p, BorderLayout.CENTER);
        dialog.add(bp, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateStatus(String newStatus) {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String ref = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Override status of booking '" + ref + "' to " + newStatus + " in the database?",
                "Confirm Status Override",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = bookingService.updateBookingStatus(id, newStatus);
            if (ok) {
                tableModel.setValueAt(newStatus, row, 7);
                JOptionPane.showMessageDialog(this, "Booking '" + ref + "' status set to " + newStatus + " in database.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update booking status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePayment(String newPayment) {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String ref = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Override payment status of booking '" + ref + "' to " + newPayment + " in the database?",
                "Confirm Payment Override",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = bookingService.updatePaymentStatus(id, newPayment);
            if (ok) {
                tableModel.setValueAt(newPayment, row, 8);
                JOptionPane.showMessageDialog(this, "Payment status of '" + ref + "' set to " + newPayment + " in database.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update payment status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
