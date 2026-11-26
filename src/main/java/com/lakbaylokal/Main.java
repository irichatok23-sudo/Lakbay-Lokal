package com.lakbaylokal;

import com.lakbaylokal.config.AppConfig;
import com.lakbaylokal.database.DatabaseInitializer;
import com.lakbaylokal.server.LakbayServer;
import com.lakbaylokal.ui.LakbaySwingLauncher;

import javax.swing.*;
import java.net.BindException;

/**
 * Main Entrypoint for Lakbay Lokal: A Community-Based Tourism Information System.
 * Initializes the database schema, seeds Biliran tourism data, starts the embedded HTTP
 * server, and launches the Java Swing Desktop Controller.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  LAKBAY LOKAL: Community-Based Tourism Information System");
        System.out.println("  Province of Biliran, Philippines                        ");
        System.out.println("==========================================================");

        boolean webOnly = false;
        if (args != null) {
            for (String arg : args) {
                if ("--web-only".equalsIgnoreCase(arg) || "--headless".equalsIgnoreCase(arg)) {
                    webOnly = true;
                    break;
                }
            }
        }

        // 1. Set native OS look and feel for Swing
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // 2. Initialize MySQL schema & seed data if MySQL is running
        System.out.println("[Boot] Initializing MySQL database & Biliran tourism dataset...");
        DatabaseInitializer.initializeIfNeeded();

        // 3. Start Embedded HTTP Server for full-featured interactive UI
        int port = AppConfig.getServerPort();
        LakbayServer server = new LakbayServer();
        boolean serverStartedHere = false;
        try {
            server.start();
            serverStartedHere = true;
        } catch (BindException bindEx) {
            System.out.println("[Server] Port " + port + " is already in use by an active Lakbay Lokal instance.");
            System.out.println("[Server] Connecting to existing server at http://localhost:" + port + "/");
        } catch (Exception e) {
            System.err.println("[Error] Could not bind HTTP server on port " + port + ": " + e.getMessage());
        }

        // 4. Launch Desktop Swing Controller (without auto-opening a web browser) unless running in headless mode
        if (!webOnly) {
            final boolean startedLocal = serverStartedHere;
            SwingUtilities.invokeLater(() -> {
                LakbaySwingLauncher launcher = new LakbaySwingLauncher(port);
                launcher.setVisible(true);
                if (!startedLocal) {
                    launcher.appendLog("Attached to existing Lakbay Lokal server on port " + port + ".");
                }
                launcher.launchStandaloneDesktopApp();
            });
        } else {
            System.out.println("[Boot] Running in headless REST API server mode on http://localhost:" + port + "/");
        }
    }
}
