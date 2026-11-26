package lakbaylokal.stakeholder.schedules;

import lakbaylokal.shared.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class StakeholderSchedulesPanel extends JPanel {
    private JTable scheduleTable;
    private DefaultTableModel tableModel;

    public StakeholderSchedulesPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadInitialSchedules();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Tour & Trip Schedules");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Organize daily departure slots, vessel dispatch, guide shifts, and slot availability.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.COLOR_TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JButton addSlotBtn = UITheme.createSuccessButton("+ Add Schedule Slot");
        addSlotBtn.addActionListener(e -> showAddScheduleDialog());

        header.add(textPanel, BorderLayout.WEST);
        header.add(addSlotBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent createMainContent() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setOpaque(false);

        String[] cols = {"Slot ID", "Date", "Time Slot", "Route / Activity", "Max Pax", "Booked Pax", "Available Slots", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        scheduleTable = new JTable(tableModel);
        UITheme.styleTable(scheduleTable);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footer.setOpaque(false);

        JButton markFullBtn = UITheme.createWarningButton("Mark as Closed / Full");
        markFullBtn.addActionListener(e -> {
            int row = scheduleTable.getSelectedRow();
            if (row >= 0) {
                tableModel.setValueAt("CLOSED", row, 7);
                JOptionPane.showMessageDialog(this, "Slot marked as CLOSED.", "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton cancelSlotBtn = UITheme.createDangerButton("Cancel Departure");
        cancelSlotBtn.addActionListener(e -> {
            int row = scheduleTable.getSelectedRow();
            if (row >= 0) {
                tableModel.setValueAt("WEATHER CANCELLED", row, 7);
                JOptionPane.showMessageDialog(this, "Departure slot cancelled due to weather/advisory.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        footer.add(markFullBtn);
        footer.add(cancelSlotBtn);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private void loadInitialSchedules() {
        LocalDate today = LocalDate.now();
        tableModel.addRow(new Object[]{"SCH-101", today.toString(), "06:30 AM - 11:30 AM", "Sambawan Island Morning Crossing", 15, 8, 7, "OPEN"});
        tableModel.addRow(new Object[]{"SCH-102", today.toString(), "01:00 PM - 05:30 PM", "Sambawan Island Afternoon Crossing", 15, 15, 0, "FULL"});
        tableModel.addRow(new Object[]{"SCH-103", today.plusDays(1).toString(), "07:00 AM - 12:00 PM", "Tinago & Casawasan Waterfalls Trek", 10, 4, 6, "OPEN"});
        tableModel.addRow(new Object[]{"SCH-104", today.plusDays(2).toString(), "05:00 AM - 02:00 PM", "Mount Tres Marias Summit Climb", 8, 6, 2, "OPEN"});
    }

    private void showAddScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Tour Departure Slot", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField dateField = new JTextField(LocalDate.now().plusDays(3).toString());
        JTextField timeField = new JTextField("08:00 AM - 12:00 PM");
        JTextField routeField = new JTextField("Higatangan Island Sandbar Shifting Tour");
        JTextField maxPaxField = new JTextField("12");

        panel.add(new JLabel("Departure Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Operating Hours:"));
        panel.add(timeField);
        panel.add(new JLabel("Destination Route:"));
        panel.add(routeField);
        panel.add(new JLabel("Maximum Capacity:"));
        panel.add(maxPaxField);

        JButton saveBtn = UITheme.createSuccessButton("Confirm Slot");
        saveBtn.addActionListener(e -> {
            try {
                int max = Integer.parseInt(maxPaxField.getText().trim());
                tableModel.addRow(new Object[]{
                        "SCH-" + (100 + tableModel.getRowCount() + 1),
                        dateField.getText().trim(),
                        timeField.getText().trim(),
                        routeField.getText().trim(),
                        max,
                        0,
                        max,
                        "OPEN"
                });
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Schedule slot published successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid values.", "Validation", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createNeutralButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancelBtn);
        actions.add(saveBtn);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
