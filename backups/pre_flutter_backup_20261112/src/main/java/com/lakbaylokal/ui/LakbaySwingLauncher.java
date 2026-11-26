package com.lakbaylokal.ui;

import com.lakbaylokal.config.AppConfig;
import com.lakbaylokal.database.BackupRestoreService;
import com.lakbaylokal.database.ConnectionFactory;
import com.lakbaylokal.database.DatabaseInitializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Java Swing Desktop Controller & Launcher window for Lakbay Lokal.
 * Displays system health, database connectivity, quick links, and embedded interface launcher.
 */
public class LakbaySwingLauncher extends JFrame {

    private final JLabel lblDbStatus = new JLabel("Checking database...");
    private final JLabel lblServerStatus = new JLabel("Server Active");
    private final JTextArea txtLogs = new JTextArea();
    private final int port;

    public LakbaySwingLauncher(int port) {
        this.port = port;
        initUI();
        checkDatabaseStatus();
    }

    private void initUI() {
        setTitle("Lakbay Lokal: Community-Based Tourism Information System");
        setSize(780, 560);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 250));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("LAKBAY LOKAL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(16, 85, 59)); // Forest Emerald

        JLabel subtitleLabel = new JLabel("A Community-Based Tourism Information System — Biliran, Philippines");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(80, 95, 105));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Center Cards Panel
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);

        // Status Card
        JPanel statusCard = createCardPanel("System Health & Status");
        statusCard.setLayout(new GridLayout(4, 1, 8, 8));

        lblServerStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblServerStatus.setText("● Web Engine: Active on port " + port);
        lblServerStatus.setForeground(new Color(16, 140, 70));

        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblLocale = new JLabel("● Region: Biliran Province (8 Municipalities)");
        lblLocale.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLocale.setForeground(new Color(50, 60, 70));

        JLabel lblRoles = new JLabel("● Active Roles: 6 (Tourists, Stakeholders, Brgy, LGU, Tourism, Admin)");
        lblRoles.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRoles.setForeground(new Color(70, 80, 90));

        statusCard.add(lblServerStatus);
        statusCard.add(lblDbStatus);
        statusCard.add(lblLocale);
        statusCard.add(lblRoles);

        // Actions Card
        JPanel actionsCard = createCardPanel("Quick Actions & Launcher");
        actionsCard.setLayout(new GridLayout(4, 1, 8, 8));

        JButton btnOpenApp = new JButton("🚀 Launch Lakbay Lokal Desktop UI");
        btnOpenApp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOpenApp.setBackground(new Color(16, 120, 80));
        btnOpenApp.setForeground(Color.WHITE);
        btnOpenApp.setFocusPainted(false);
        btnOpenApp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpenApp.addActionListener(e -> openBrowser("http://localhost:" + port));

        JButton btnTestDb = new JButton("🔍 Test MySQL Connection");
        btnTestDb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTestDb.addActionListener(e -> checkDatabaseStatus());

        JButton btnBackup = new JButton("💾 Create Database Backup (.sql)");
        btnBackup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBackup.addActionListener(e -> handleBackup());

        JButton btnGuide = new JButton("🔑 Sample Accounts & Credentials");
        btnGuide.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGuide.addActionListener(e -> showCredentialsDialog());

        actionsCard.add(btnOpenApp);
        actionsCard.add(btnTestDb);
        actionsCard.add(btnBackup);
        actionsCard.add(btnGuide);

        centerPanel.add(statusCard);
        centerPanel.add(actionsCard);

        // Bottom Activity Log Console
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setOpaque(false);

        JLabel lblLogTitle = new JLabel("Activity & Diagnostics Log");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLogTitle.setForeground(new Color(60, 70, 80));

        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtLogs.setBackground(new Color(25, 30, 36));
        txtLogs.setForeground(new Color(210, 225, 235));
        txtLogs.setRows(6);
        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        bottomPanel.add(lblLogTitle, BorderLayout.NORTH);
        bottomPanel.add(scrollLogs, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        appendLog("Lakbay Lokal Desktop System initialized.");
        appendLog("Desktop HTTP server listening on http://localhost:" + port + "/");
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 235), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }

    public void checkDatabaseStatus() {
        boolean reachable = ConnectionFactory.testConnection();
        if (reachable) {
            lblDbStatus.setText("● Database: Connected (MySQL @" + AppConfig.getDbHost() + ":" + AppConfig.getDbPort() + ")");
            lblDbStatus.setForeground(new Color(16, 140, 70));
            appendLog("Database check SUCCESS: Connected to " + AppConfig.getDbName());
        } else {
            lblDbStatus.setText("● Database: Offline / Unreachable (Check XAMPP/MySQL)");
            lblDbStatus.setForeground(new Color(200, 40, 40));
            appendLog("Database check WARNING: Unable to connect to MySQL on localhost:3306. Please start MySQL via XAMPP or WAMP.");
        }
    }

    private void handleBackup() {
        try {
            File backup = BackupRestoreService.createBackup();
            appendLog("Backup created successfully: " + backup.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Database backup completed successfully!\nFile: " + backup.getName() +
                            "\nSaved to: " + backup.getParentFile().getAbsolutePath(),
                    "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            appendLog("Backup failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Failed to create database backup:\n" + ex.getMessage() +
                            "\nEnsure MySQL is running.",
                    "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCredentialsDialog() {
        String msg = "Pre-Seeded Test Accounts (Password for all: password123):\n\n" +
                "1. System Administrator:\n" +
                "   Username: admin\n\n" +
                "2. Tourism Office:\n" +
                "   Username: tourismoffice\n\n" +
                "3. LGU Officer (Naval):\n" +
                "   Username: lgu_naval\n\n" +
                "4. Barangay Official (Ol-og / Sambawan):\n" +
                "   Username: brgy_olog\n\n" +
                "5. Local Stakeholders:\n" +
                "   Tour Guide: guide_pedro\n" +
                "   Transport: driver_juan\n" +
                "   Boat Operator: boat_capitan\n" +
                "   Cook / Catering: cook_nanay_luz\n" +
                "   Artisan: artisan_rosa\n\n" +
                "6. Tourist:\n" +
                "   Username: tourist_ana\n";

        JOptionPane.showMessageDialog(this, msg, "Sample Accounts & Test Roles", JOptionPane.INFORMATION_MESSAGE);
    }

    public void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                appendLog("Launched interface in default browser: " + url);
            } else {
                appendLog("Desktop browser launch not supported by OS. Please open " + url + " manually.");
            }
        } catch (Exception ex) {
            appendLog("Failed to open browser: " + ex.getMessage());
        }
    }

    public void appendLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        txtLogs.append("[" + time + "] " + message + "\n");
        txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
    }
}
