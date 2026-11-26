package lakbaylokal.tourist.itinerary;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.Booking;
import lakbaylokal.model.BookingItem;
import lakbaylokal.model.TourismService;
import lakbaylokal.model.TouristProfile;
import lakbaylokal.model.User;
import lakbaylokal.service.BookingService;
import lakbaylokal.service.StakeholderService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.tourist.TouristDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TouristItineraryPanel extends JPanel {

    private final StakeholderService stakeholderService = new StakeholderService();
    private final BookingService bookingService = new BookingService();
    private final TouristDashboard parentDashboard;

    private final JSpinner spinPax = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
    private final JTextField txtStartDate = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
    private final JTextField txtEndDate = new JTextField(LocalDate.now().plusDays(2).toString(), 10);
    private final JTextField txtNotes = new JTextField("Eco-trekking & island adventure", 20);
    private final JComboBox<String> cmbCategory = new JComboBox<>(new String[]{"ALL", "GUIDING", "TRANSPORT", "BOAT_CHARTER", "FOOD_CATERING", "HANDICRAFT"});

    private final JPanel servicesChecklistPanel = new JPanel();
    private final JLabel lblTotal = new JLabel("₱0.00");
    private final JLabel lblPerPax = new JLabel("₱0.00");
    private final JLabel lblSelectedCount = new JLabel("0 services selected");

    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private final List<TourismService> availableServices = new ArrayList<>();

    public TouristItineraryPanel() {
        this(null);
    }

    public TouristItineraryPanel(TouristDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Center / Left: Travel parameters & Services checklist
        JPanel leftPanel = new JPanel(new BorderLayout(12, 12));
        leftPanel.setOpaque(false);

        // Parameters Card
        JPanel paramsCard = UITheme.createCardPanel("Travel Parameters & Schedule");
        JPanel formGrid = new JPanel(new GridLayout(2, 4, 10, 10));
        formGrid.setOpaque(false);

        formGrid.add(new JLabel("Travel Start (YYYY-MM-DD):"));
        formGrid.add(txtStartDate);
        formGrid.add(new JLabel("Travel End (YYYY-MM-DD):"));
        formGrid.add(txtEndDate);

        formGrid.add(new JLabel("Number of Travelers (Pax):"));
        spinPax.addChangeListener(e -> recalculateTotal());
        formGrid.add(spinPax);
        formGrid.add(new JLabel("Trip Notes / Preferences:"));
        formGrid.add(txtNotes);

        paramsCard.add(formGrid, BorderLayout.CENTER);
        leftPanel.add(paramsCard, BorderLayout.NORTH);

        // Services Card
        JPanel servicesCard = UITheme.createCardPanel("Select Community Stakeholder Services");
        
        JPanel filterHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterHeader.setOpaque(false);
        filterHeader.add(new JLabel("Category Filter:"));
        cmbCategory.setFont(UITheme.FONT_BODY);
        cmbCategory.addActionListener(e -> loadServices());
        filterHeader.add(cmbCategory);

        JButton btnSelectAll = UITheme.createSecondaryButton("Select All");
        btnSelectAll.addActionListener(e -> {
            for (JCheckBox cb : checkBoxes) cb.setSelected(true);
            recalculateTotal();
        });
        filterHeader.add(btnSelectAll);

        JButton btnClearAll = UITheme.createSecondaryButton("Clear Selection");
        btnClearAll.addActionListener(e -> {
            for (JCheckBox cb : checkBoxes) cb.setSelected(false);
            recalculateTotal();
        });
        filterHeader.add(btnClearAll);

        servicesChecklistPanel.setLayout(new BoxLayout(servicesChecklistPanel, BoxLayout.Y_AXIS));
        servicesChecklistPanel.setBackground(Color.WHITE);
        servicesChecklistPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JScrollPane scrollServices = new JScrollPane(servicesChecklistPanel);
        scrollServices.setPreferredSize(new Dimension(540, 280));
        scrollServices.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        servicesCard.add(filterHeader, BorderLayout.NORTH);
        servicesCard.add(scrollServices, BorderLayout.CENTER);
        leftPanel.add(servicesCard, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);

        // Right Panel: Estimated Budget & Reservation Card
        JPanel summaryCard = UITheme.createCardPanel("Estimated Budget");
        summaryCard.setPreferredSize(new Dimension(280, 0));
        JPanel sumBox = new JPanel();
        sumBox.setLayout(new BoxLayout(sumBox, BoxLayout.Y_AXIS));
        sumBox.setOpaque(false);

        JLabel lblSumTitle = new JLabel("ESTIMATED TOTAL:");
        lblSumTitle.setFont(UITheme.FONT_SMALL);
        lblSumTitle.setForeground(UITheme.TEXT_MUTED);

        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotal.setForeground(UITheme.PRIMARY);

        JLabel lblPerPaxTitle = new JLabel("PER PERSON SHARE:");
        lblPerPaxTitle.setFont(UITheme.FONT_SMALL);
        lblPerPaxTitle.setForeground(UITheme.TEXT_MUTED);

        lblPerPax.setFont(UITheme.FONT_HEADER);
        lblPerPax.setForeground(UITheme.TEAL);

        lblSelectedCount.setFont(UITheme.FONT_BODY);
        lblSelectedCount.setForeground(UITheme.TEXT_MUTED);

        JButton btnBook = UITheme.createSuccessButton("Reserve Custom Trip →");
        btnBook.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnBook.addActionListener(e -> submitCustomBooking());

        sumBox.add(lblSumTitle);
        sumBox.add(Box.createVerticalStrut(4));
        sumBox.add(lblTotal);
        sumBox.add(Box.createVerticalStrut(12));
        sumBox.add(lblPerPaxTitle);
        sumBox.add(Box.createVerticalStrut(2));
        sumBox.add(lblPerPax);
        sumBox.add(Box.createVerticalStrut(12));
        sumBox.add(lblSelectedCount);
        sumBox.add(Box.createVerticalStrut(20));
        sumBox.add(btnBook);

        summaryCard.add(sumBox, BorderLayout.NORTH);
        add(summaryCard, BorderLayout.EAST);

        loadServices();
    }

    public void loadServices() {
        servicesChecklistPanel.removeAll();
        checkBoxes.clear();
        availableServices.clear();

        String cat = (String) cmbCategory.getSelectedItem();
        List<TourismService> list = stakeholderService.getAllAvailableServices(cat);
        availableServices.addAll(list);

        for (TourismService svc : list) {
            String unitText = "PER_PERSON".equalsIgnoreCase(svc.getPricingUnit()) ? "/ person" : "/ trip";
            String provider = (svc.getBusinessName() != null) ? svc.getBusinessName() : "Local Community Partner";
            JCheckBox cb = new JCheckBox("<html><b>" + svc.getServiceName() + "</b> — <span style='color:#047857;'>₱" +
                    svc.getBasePrice() + " " + unitText + "</span> <span style='color:#64748b;'>(" + provider + ")</span></html>");
            cb.setFont(UITheme.FONT_BODY);
            cb.setBackground(Color.WHITE);
            cb.setBorder(new EmptyBorder(6, 4, 6, 4));
            cb.addActionListener(e -> recalculateTotal());
            checkBoxes.add(cb);
            servicesChecklistPanel.add(cb);
        }

        servicesChecklistPanel.revalidate();
        servicesChecklistPanel.repaint();
        recalculateTotal();
    }

    private void recalculateTotal() {
        int pax = (int) spinPax.getValue();
        double total = 0;
        int count = 0;

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                count++;
                TourismService svc = availableServices.get(i);
                double base = svc.getBasePrice().doubleValue();
                if ("PER_PERSON".equalsIgnoreCase(svc.getPricingUnit())) {
                    total += (base * pax);
                } else {
                    total += base;
                }
            }
        }

        lblSelectedCount.setText(count + " service(s) selected");
        lblTotal.setText(String.format("₱%,.2f", total));
        double perPax = pax > 0 ? (total / pax) : 0;
        lblPerPax.setText(String.format("₱%,.2f", perPax));
    }

    private void submitCustomBooking() {
        int pax = (int) spinPax.getValue();
        List<BookingItem> items = new ArrayList<>();
        double grandTotal = 0;

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                TourismService svc = availableServices.get(i);
                double base = svc.getBasePrice().doubleValue();
                double sub = "PER_PERSON".equalsIgnoreCase(svc.getPricingUnit()) ? (base * pax) : base;
                grandTotal += sub;

                BookingItem item = new BookingItem();
                item.setServiceId(svc.getServiceId());
                item.setStakeholderId(svc.getStakeholderId());
                item.setUnitPrice(svc.getBasePrice());
                item.setQuantity("PER_PERSON".equalsIgnoreCase(svc.getPricingUnit()) ? pax : 1);
                item.setSubtotal(BigDecimal.valueOf(sub));
                item.setStatus("PENDING");
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one community service from the checklist.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = SessionManager.getCurrentUser();
        TouristProfile tp = (user != null) ? bookingService.getTouristProfileByUserId(user.getUserId()) : null;
        int touristId = (tp != null) ? tp.getProfileId() : 1;

        Booking b = new Booking();
        b.setTouristId(touristId);
        try {
            Date start = Date.valueOf(txtStartDate.getText().trim());
            Date end = Date.valueOf(txtEndDate.getText().trim());
            if (end.before(start)) {
                JOptionPane.showMessageDialog(this, "End date cannot be earlier than start date.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            b.setTravelDateStart(start);
            b.setTravelDateEnd(end);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD (e.g. " + LocalDate.now() + ")", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        b.setNumberOfPax(pax);
        b.setTotalAmount(BigDecimal.valueOf(grandTotal));
        b.setStatus("PENDING");
        b.setPaymentStatus("UNPAID");
        b.setCustomNotes("Custom Trip: " + txtNotes.getText().trim());

        int id = bookingService.createBooking(b, items);
        if (id > 0) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "🎉 Custom Experience Reserved Successfully!\n\n" +
                            "Booking Reference: " + b.getBookingReference() + "\n" +
                            "Estimated Amount: " + String.format("₱%,.2f", grandTotal) + "\n" +
                            "Included Services: " + items.size() + " community services\n\n" +
                            "Would you like to view this reservation in 'My Bookings'?",
                    "Success",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"View My Bookings", "Create Another"},
                    "View My Bookings"
            );

            for (JCheckBox cb : checkBoxes) cb.setSelected(false);
            recalculateTotal();

            if (choice == 0 && parentDashboard != null) {
                parentDashboard.switchToTab(2); // My Bookings tab
            }
        } else {
            JOptionPane.showMessageDialog(this, "Booking could not be saved to database. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
