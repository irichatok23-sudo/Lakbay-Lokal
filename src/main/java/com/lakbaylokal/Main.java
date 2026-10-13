package com.lakbaylokal;

import com.lakbaylokal.config.AppConfig;
import com.lakbaylokal.database.DatabaseInitializer;
import com.lakbaylokal.server.LakbayServer;
import com.lakbaylokal.ui.LakbaySwingLauncher;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

/**
 * Main application bootstrap for Lakbay Lokal.
 * Initializes the database schema, launches the embedded HTTP service,
 * displays the Java Swing desktop window, and opens the modern Javascript interface.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("  LAKBAY LOKAL: A COMMUNITY-BASED TOURISM INFORMATION SYSTEM");
        System.out.println("  Target Locale: Biliran Province, Philippines");
        System.out.println("  Version: " + AppConfig.getAppVersion());
        System.out.println("====================================================================");

        // 1. Initialize Database
        try {
            System.out.println("[Bootstrap] Verifying database connection and schema...");
            boolean dbReady = DatabaseInitializer.initializeIfNeeded();
            if (dbReady) {
                System.out.println("[Bootstrap] Database connection established and tables ready.");
            } else {
                System.out.println("[Bootstrap] Note: MySQL not yet running on localhost:3306.");
                System.out.println("[Bootstrap] You can start MySQL via XAMPP / WAMP and click 'Test Connection' in the launcher.");
            }
        } catch (Exception e) {
            System.err.println("[Bootstrap] Database init notice: " + e.getMessage());
        }

        // 2. Start Embedded Web & API Server
        LakbayServer server = new LakbayServer();
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("[Bootstrap] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 3. Register Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Bootstrap] Shutting down Lakbay Lokal...");
            server.stop();
        }));

        // 4. Launch Desktop Swing Window and Open Web UI
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
                LakbaySwingLauncher launcher = new LakbaySwingLauncher(server.getPort());
                launcher.setVisible(true);
                // Launch default browser
                launcher.openBrowser("http://localhost:" + server.getPort() + "/");
            });
        } else {
            System.out.println("[Bootstrap] Running in headless environment. Open http://localhost:" + server.getPort() + "/ in your browser.");
        }
    }
}
