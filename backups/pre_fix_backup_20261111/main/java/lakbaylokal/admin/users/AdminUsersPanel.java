package lakbaylokal.admin.users;

import lakbaylokal.model.Role;
import lakbaylokal.model.User;
import lakbaylokal.service.UserService;
import lakbaylokal.shared.UITheme;
import lakbaylokal.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUsersPanel extends JPanel {
    private final UserService userService = new UserService();

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;

    public AdminUsersPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableSection(), BorderLayout.CENTER);

        loadUsers();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("System User Accounts Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage credentials, assign system roles, and activate/deactivate user accounts across all 6 roles.");
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
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCSV(usersTable, "Admin_User_Accounts"));

        JButton addBtn = UITheme.createSuccessButton("+ Add New User");
        addBtn.addActionListener(e -> showAddUserDialog());

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh List");
        refreshBtn.addActionListener(e -> loadUsers());

        actionPanel.add(exportBtn);
        actionPanel.add(addBtn);
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
        searchField.putClientProperty("JTextField.placeholderText", "Username, name, email...");
        filterBar.add(searchField);

        filterBar.add(new JLabel("Role Filter:"));
        roleFilterCombo = new JComboBox<>(new String[]{
                "ALL ROLES",
                "SYSTEM_ADMIN",
                "TOURISM_OFFICE",
                "LGU_OFFICER",
                "BARANGAY_OFFICIAL",
                "STAKEHOLDER",
                "TOURIST"
        });
        filterBar.add(roleFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.addActionListener(e -> loadUsers());
        filterBar.add(filterBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            roleFilterCombo.setSelectedIndex(0);
            loadUsers();
        });
        filterBar.add(resetBtn);

        // Table
        String[] cols = {"User ID", "Username", "Full Name", "Email Address", "Contact", "Assigned Role", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        usersTable = new JTable(tableModel);
        UITheme.styleTable(usersTable);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER));

        // Action Buttons
        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        footerActions.setOpaque(false);

        JButton toggleStatusBtn = UITheme.createWarningButton("Toggle Active / Inactive");
        toggleStatusBtn.addActionListener(e -> toggleUserStatus());

        JButton changeRoleBtn = UITheme.createPrimaryButton("Change Role");
        changeRoleBtn.addActionListener(e -> changeUserRole());

        JButton deleteUserBtn = UITheme.createDangerButton("Delete User");
        deleteUserBtn.addActionListener(e -> deleteSelectedUser());

        footerActions.add(toggleStatusBtn);
        footerActions.add(changeRoleBtn);
        footerActions.add(deleteUserBtn);

        container.add(filterBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footerActions, BorderLayout.SOUTH);
        return container;
    }

    public void loadUsers() {
        tableModel.setRowCount(0);
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String roleFilter = (String) (roleFilterCombo != null ? roleFilterCombo.getSelectedItem() : "ALL ROLES");

        List<User> list = userService.getAllUsers();
        for (User u : list) {
            String roleStr = u.getRoleName() != null ? u.getRoleName() : (u.getRoleCode() != null ? u.getRoleCode() : "Role " + u.getRoleId());
            if (roleFilter != null && !roleFilter.equalsIgnoreCase("ALL ROLES")) {
                String roleCode = u.getRoleCode() != null ? u.getRoleCode() : "";
                if (!roleCode.equalsIgnoreCase(roleFilter) && !roleStr.toUpperCase().contains(roleFilter)) {
                    continue;
                }
            }
            if (!kw.isEmpty()) {
                String combined = (u.getUsername() + " " + u.getFullName() + " " + u.getEmail() + " " + roleStr).toLowerCase();
                if (!combined.contains(kw)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhoneNumber() != null ? u.getPhoneNumber() : "N/A",
                    roleStr,
                    u.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create System User Account", true);
        dialog.setSize(500, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField("+63 9");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{
                "1 - SYSTEM_ADMIN (Administrator)",
                "2 - TOURISM_OFFICE (Provincial Office)",
                "3 - LGU_OFFICER (Municipal Desk)",
                "4 - BARANGAY_OFFICIAL (Barangay Tourism)",
                "5 - STAKEHOLDER (Enterprise / Guide)",
                "6 - TOURIST (Public Traveler)"
        });

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email Address:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("System Role:"));
        panel.add(roleCombo);

        JButton saveBtn = UITheme.createPrimaryButton("Save User");
        saveBtn.addActionListener(e -> {
            String u = usernameField.getText().trim();
            String p = new String(passwordField.getPassword()).trim();
            String n = fullNameField.getText().trim();
            String em = emailField.getText().trim();

            if (u.isEmpty() || p.isEmpty() || n.isEmpty() || em.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int roleId = Integer.parseInt(((String) roleCombo.getSelectedItem()).split(" - ")[0]);

            User newUser = new User();
            newUser.setUsername(u);
            newUser.setFullName(n);
            newUser.setEmail(em);
            newUser.setPhoneNumber(phoneField.getText().trim());
            newUser.setRoleId(roleId);
            newUser.setStatus("ACTIVE");

            boolean ok = userService.registerUser(newUser, p);
            if (ok) {
                dialog.dispose();
                loadUsers();
                JOptionPane.showMessageDialog(this, "User created successfully with role ID " + roleId + " in database!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create user (username or email may already exist).", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(saveBtn);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void toggleUserStatus() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        String cur = (String) tableModel.getValueAt(row, 6);
        String next = cur.equals("ACTIVE") ? "INACTIVE" : "ACTIVE";

        boolean ok = userService.updateStatus(userId, next);
        if (ok) {
            tableModel.setValueAt(next, row, 6);
            JOptionPane.showMessageDialog(this, "User '" + username + "' status updated to " + next + " in database.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update user status in database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeUserRole() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        User u = userService.getUserById(userId);
        if (u == null) return;

        String[] roles = {
                "1 - SYSTEM_ADMIN",
                "2 - TOURISM_OFFICE",
                "3 - LGU_OFFICER",
                "4 - BARANGAY_OFFICIAL",
                "5 - STAKEHOLDER",
                "6 - TOURIST"
        };

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select new role for user '" + username + "':",
                "Reassign Role in Database",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[Math.min(Math.max(u.getRoleId() - 1, 0), roles.length - 1)]
        );

        if (selected != null) {
            int newRoleId = Integer.parseInt(selected.split(" - ")[0]);
            u.setRoleId(newRoleId);
            boolean ok = userService.updateUser(u);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Role for '" + username + "' changed to " + selected + " in database.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user role in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        if (userId == 1) {
            JOptionPane.showMessageDialog(this, "Master administrator account (ID #1) cannot be deleted.", "Security Guard", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete user '" + username + "' (ID #" + userId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = userService.deleteUser(userId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "User deleted successfully from database.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user. The user may have associated booking records.", "Database Constraint", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
