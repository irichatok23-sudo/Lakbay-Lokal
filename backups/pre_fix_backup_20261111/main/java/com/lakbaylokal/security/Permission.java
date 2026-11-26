package com.lakbaylokal.security;

/**
 * Granular permissions catalog for Lakbay Lokal Role-Based Access Control (RBAC).
 */
public enum Permission {
    // Destinations
    DESTINATION_VIEW,
    DESTINATION_CREATE,
    DESTINATION_UPDATE,
    DESTINATION_DELETE,
    DESTINATION_VALIDATE,

    // Packages & Circuits
    PACKAGE_VIEW,
    PACKAGE_CREATE,
    PACKAGE_UPDATE,
    PACKAGE_DELETE,

    // Stakeholder Services
    SERVICE_VIEW,
    SERVICE_MANAGE_OWN,
    SERVICE_MANAGE_ALL,

    // Stakeholder Profiles & Verification
    STAKEHOLDER_VIEW,
    STAKEHOLDER_MANAGE_OWN,
    STAKEHOLDER_VERIFY,

    // Bookings & Reservations
    BOOKING_CREATE,
    BOOKING_VIEW_OWN,
    BOOKING_VIEW_JURISDICTION,
    BOOKING_VIEW_ALL,
    BOOKING_UPDATE_STATUS,

    // Reviews & Testimonials
    REVIEW_VIEW,
    REVIEW_CREATE,
    REVIEW_MANAGE,

    // Announcements
    ANNOUNCEMENT_VIEW,
    ANNOUNCEMENT_PUBLISH,
    ANNOUNCEMENT_DELETE,

    // Reports & Analytics
    REPORT_RECORD_ARRIVAL,
    REPORT_VIEW_BARANGAY,
    REPORT_VIEW_MUNICIPALITY,
    REPORT_VIEW_PROVINCIAL,

    // System Administration
    USER_MANAGE,
    AUDIT_VIEW,
    DATABASE_MANAGE
}
