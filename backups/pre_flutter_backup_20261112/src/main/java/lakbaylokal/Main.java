package lakbaylokal;

import lakbaylokal.authentication.LoginFrame;
import lakbaylokal.database.DatabaseInitializer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  LAKBAY LOKAL: COMMUNITY-BASED TOURISM INFORMATION SYSTEM       ");
        System.out.println("  Target Deployment: Biliran Province, Philippines              ");
        System.out.println("==================================================================");

        // Set modern Look & Feel
        lakbaylokal.shared.UITheme.initializeLookAndFeel();

        // Initialize Database Schema and Seeds if needed
        try {
            System.out.println("Connecting to database & verifying table schema...");
            DatabaseInitializer.initializeIfNeeded();
            System.out.println("Database initialization routine completed.");
        } catch (Exception e) {
            System.err.println("Database Notice: " + e.getMessage());
            System.out.println("System will run with standard embedded demo and fallback support.");
        }

        // Start Embedded Web & REST API Server (serves http://localhost:8080 if available)
        try {
            Class<?> serverCls = Class.forName("com.lakbaylokal.server.LakbayServer");
            Object serverObj = serverCls.getDeclaredConstructor().newInstance();
            serverCls.getMethod("start").invoke(serverObj);
            System.out.println("[Web Server] Access web portal at: http://localhost:8080/");
        } catch (ReflectiveOperationException ignored) {
            // Embedded web server is optional
        }

        // Launch Login Frame on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
