package lakbaylokal.stakeholder.services;

import lakbaylokal.authentication.SessionManager;
import lakbaylokal.model.StakeholderProfile;
import lakbaylokal.model.TourismService;
import lakbaylokal.model.User;
import lakbaylokal.service.StakeholderService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StakeholderServicesPanel extends JPanel {
    private final StakeholderService stakeholderService = new StakeholderService();

    private JTable servicesTable;
    private DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbCategory;

    private List<TourismService> serviceList = new ArrayList<>();
    private int currentStakeholderProfileId = 3; // Default Maripipi Boats

    public StakeholderServicesPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top Filter & Action Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.setOpaque(false);

        filters.add(new JLabel("🔍 Search:"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.addActionListener(e -> applyFilter());
        filters.add(txtSearch);

        filters.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"ALL", "BOAT_CHARTER", "GUIDING", "TRANSPORT", "FOOD_CATERING", "HANDICRAFT"});
        cmbCategory.setFont(UITheme.FONT_BODY);
        cmbCategory.addActionListener(e -> applyFilter());
        filters.add(cmbCategory);

        JButton btnFilter = UITheme.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> applyFilter());
        filters.add(btnFilter);

        JButton btnReset = UITheme.createSecondaryButton("Refresh");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            loadServices();
        });
        filters.add(btnReset);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);

        JButton addBtn = UITheme.createSuccessButton("+ Add New Service");
        addBtn.addActionListener(e -> showAddOrEditServiceDialog(null));
        actions.add(addBtn);

        JButton editBtn = UITheme.createPrimaryButton("Edit Service");
        editBtn.addActionListener(e -> editSelectedService());
        actions.add(editBtn);

        JButton exportBtn = UITheme.createSecondaryButton("📥 Export");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(this, servicesTable, "Stakeholder_Services", "Stakeholder Community Services"));
        actions.add(exportBtn);

        topBar.add(filters, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Data Table
        String[] cols = {"ID", "Service Name", "Category", "Rate / Price", "Pricing Unit", "Max Pax", "Availability", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        servicesTable = UITheme.createStyledTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(servicesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        // Bottom Controls
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomBar.setOpaque(false);

        JButton toggleBtn = UITheme.createWarningButton("Toggle Open / Closed Availability");
        toggleBtn.addActionListener(e -> toggleAvailability());

        JButton deleteBtn = UITheme.createDangerButton("Remove Service");
        deleteBtn.addActionListener(e -> deleteSelectedService());

        bottomBar.add(toggleBtn);
        bottomBar.add(deleteBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        loadServices();
    }

    public void loadServices() {
        resolveCurrentStakeholderProfileId();
        serviceList = stakeholderService.getServicesByStakeholder(currentStakeholderProfileId);
        if (serviceList.isEmpty()) {
            // Also check all available services
            serviceList = stakeholderService.getAllAvailableServices("ALL");
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
        currentStakeholderProfileId = 3; // Default Capitan Danilo Flores profile ID
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String search = txtSearch.getText().trim().toLowerCase();
        String cat = (String) cmbCategory.getSelectedItem();

        for (TourismService s : serviceList) {
            String name = (s.getServiceName() != null ? s.getServiceName() : "").toLowerCase();
            String c = (s.getServiceCategory() != null ? s.getServiceCategory() : "");

            if (!search.isEmpty() && !name.contains(search)) {
                continue;
            }
            if (cat != null && !cat.equals("ALL") && !cat.equalsIgnoreCase(c)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    s.getServiceId(),
                    s.getServiceName(),
                    s.getServiceCategory(),
                    "₱" + s.getBasePrice(),
                    s.getPricingUnit() != null ? s.getPricingUnit() : "PER_PERSON",
                    s.getMaxCapacity() + " pax",
                    s.isAvailable() ? "ACTIVE" : "CLOSED",
                    s.getDescription() != null ? s.getDescription() : "Community-managed tourism offering."
            });
        }
    }

    private TourismService getSelectedService() {
        int row = servicesTable.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        for (TourismService s : serviceList) {
            if (s.getServiceId() == id) return s;
        }
        return stakeholderService.getServiceById(id);
    }

    private void editSelectedService() {
        TourismService selected = getSelectedService();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a service from the table to edit.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        showAddOrEditServiceDialog(selected);
    }

    private void showAddOrEditServiceDialog(TourismService existing) {
        boolean isEdit = (existing != null);
        String dialogTitle = isEdit ? "Edit Service Offering — " + existing.getServiceName() : "Add New Community Service Offering";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), dialogTitle, true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(UITheme.BG_LIGHT);

        JTextField nameField = new JTextField(isEdit ? existing.getServiceName() : "");
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"BOAT_CHARTER", "GUIDING", "TRANSPORT", "FOOD_CATERING", "HANDICRAFT"});
        if (isEdit && existing.getServiceCategory() != null) {
            catCombo.setSelectedItem(existing.getServiceCategory());
        }

        JTextField priceField = new JTextField(isEdit ? existing.getBasePrice().toString() : "1500.00");
        JComboBox<String> unitCombo = new JComboBox<>(new String[]{"PER_PERSON", "PER_TRIP", "PER_GROUP", "PER_ITEM"});
        if (isEdit && existing.getPricingUnit() != null) {
            unitCombo.setSelectedItem(existing.getPricingUnit());
        }

        JTextField capField = new JTextField(isEdit ? String.valueOf(existing.getMaxCapacity()) : "8");
        JTextField descField = new JTextField(isEdit ? existing.getDescription() : "Includes local life vests and safety gear.");
        JCheckBox availCheck = new JCheckBox("Service Currently Available & Accepting Bookings", !isEdit || existing.isAvailable());
        availCheck.setOpaque(false);

        panel.add(new JLabel("Service Name / Title:"));
        panel.add(nameField);
        panel.add(new JLabel("Service Category:"));
        panel.add(catCombo);
        panel.add(new JLabel("Base Rate (PHP):"));
        panel.add(priceField);
        panel.add(new JLabel("Pricing Unit:"));
        panel.add(unitCombo);
        panel.add(new JLabel("Max Passenger / Capacity:"));
        panel.add(capField);
        panel.add(new JLabel("Short Description:"));
        panel.add(descField);
        panel.add(new JLabel("Operational State:"));
        panel.add(availCheck);

        JButton saveBtn = UITheme.createSuccessButton(isEdit ? "Save Changes" : "Create Service");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Service name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                int cap = Integer.parseInt(capField.getText().trim());

                TourismService s = isEdit ? existing : new TourismService();
                s.setStakeholderId(currentStakeholderProfileId);
                s.setServiceName(name);
                s.setServiceCategory((String) catCombo.getSelectedItem());
                s.setBasePrice(price);
                s.setPricingUnit((String) unitCombo.getSelectedItem());
                s.setMaxCapacity(cap);
                s.setDescription(descField.getText().trim());
                s.setAvailable(availCheck.isSelected());

                if (isEdit) {
                    stakeholderService.updateService(s);
                } else {
                    stakeholderService.addService(s);
                }

                dialog.dispose();
                loadServices();
                JOptionPane.showMessageDialog(this, "Service offering saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values for price and capacity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancelBtn);
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.add(panel, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void toggleAvailability() {
        TourismService s = getSelectedService();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a service from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean next = !s.isAvailable();
        s.setAvailable(next);
        stakeholderService.updateService(s);
        loadServices();
        JOptionPane.showMessageDialog(this, "Service status updated to: " + (next ? "ACTIVE" : "CLOSED"), "Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedService() {
        TourismService s = getSelectedService();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a service to remove.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete service: " + s.getServiceName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            stakeholderService.deleteService(s.getServiceId());
            loadServices();
            JOptionPane.showMessageDialog(this, "Service removed successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
