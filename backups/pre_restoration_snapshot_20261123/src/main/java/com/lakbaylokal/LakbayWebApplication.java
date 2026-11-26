package com.lakbaylokal;

import com.lakbaylokal.database.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Lakbay Lokal: Community-Based Tourism Information System
 * Spring Boot Application Entry Point
 * 
 * Target Locale: Biliran Province, Philippines
 * Provides REST APIs, Role-Based Access Control, and serves the modern Web Frontend.
 */
@SpringBootApplication
public class LakbayWebApplication {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("  LAKBAY LOKAL: COMMUNITY-BASED TOURISM INFORMATION SYSTEM         ");
        System.out.println("  Spring Boot Web Server (Biliran Province Tourism Network)        ");
        System.out.println("====================================================================");

        SpringApplication.run(LakbayWebApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("[Lakbay Lokal] Web server started successfully.");
        System.out.println("[Lakbay Lokal] Access web portal at: http://localhost:8080/");
        
        // Ensure database schema and seed records are verified
        try {
            DatabaseInitializer.initializeIfNeeded();
        } catch (Exception e) {
            System.err.println("[Lakbay Lokal] Database verification notice: " + e.getMessage());
        }
    }
}
