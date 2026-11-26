-- ============================================================================
-- LAKBAY LOKAL: ROLE-BASED ACCESS CONTROL (RBAC) SCHEMA EXTENSION
-- Target Location: Biliran Province, Philippines
-- ============================================================================

USE lakbay_lokal_db;

-- ----------------------------------------------------------------------------
-- 1. PERMISSIONS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permissions (
    permission_id INT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(60) NOT NULL UNIQUE,
    module VARCHAR(40) NOT NULL,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. ROLE_PERMISSIONS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. JURISDICTION EXTENSION ON USERS (For LGU Officials assigned to Municipalities)
-- ----------------------------------------------------------------------------
-- Add municipality_id to users if not exists
SET @dbname = DATABASE();
SET @tablename = "users";
SET @columnname = "municipality_id";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE users ADD COLUMN municipality_id INT NULL AFTER barangay_id, ADD CONSTRAINT fk_user_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id) ON DELETE SET NULL ON UPDATE CASCADE;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Update LGU Naval user with explicit municipality_id = 1
UPDATE users SET municipality_id = 1 WHERE username = 'lgu_naval';

-- ----------------------------------------------------------------------------
-- 4. SEED PERMISSIONS CATALOG
-- ----------------------------------------------------------------------------
INSERT INTO permissions (permission_code, module, description) VALUES
-- Destinations
('DESTINATION_VIEW', 'DESTINATIONS', 'View published eco-destinations and tourist spots'),
('DESTINATION_CREATE', 'DESTINATIONS', 'Register and publish new tourism destinations in Biliran'),
('DESTINATION_UPDATE', 'DESTINATIONS', 'Modify destination details, entrance fees, and descriptions'),
('DESTINATION_DELETE', 'DESTINATIONS', 'Remove or archive destination records'),
('DESTINATION_VALIDATE', 'DESTINATIONS', 'Validate and endorse destination within authorized jurisdiction'),

-- Packages & Circuits
('PACKAGE_VIEW', 'PACKAGES', 'Browse community-curated tour circuits and packages'),
('PACKAGE_CREATE', 'PACKAGES', 'Create new community tourism packages and itinerary circuits'),
('PACKAGE_UPDATE', 'PACKAGES', 'Edit package pricing, itinerary inclusions, and durations'),
('PACKAGE_DELETE', 'PACKAGES', 'Remove or archive community travel packages'),

-- Stakeholder Services
('SERVICE_VIEW', 'SERVICES', 'View accredited stakeholder rate cards and services'),
('SERVICE_MANAGE_OWN', 'SERVICES', 'Manage, create, and update own stakeholder service offerings'),
('SERVICE_MANAGE_ALL', 'SERVICES', 'Manage, edit, or adjust all stakeholder services province-wide'),

-- Stakeholder Profiles & Verification
('STAKEHOLDER_VIEW', 'STAKEHOLDERS', 'Access verified stakeholder registry and contact details'),
('STAKEHOLDER_MANAGE_OWN', 'STAKEHOLDERS', 'Manage personal stakeholder business profile'),
('STAKEHOLDER_VERIFY', 'STAKEHOLDERS', 'Conduct verification, inspect documents, and endorse local stakeholders'),

-- Bookings & Reservations
('BOOKING_CREATE', 'BOOKINGS', 'Submit custom and pre-designed travel bookings'),
('BOOKING_VIEW_OWN', 'BOOKINGS', 'View own tourist reservations or stakeholder assigned bookings'),
('BOOKING_VIEW_JURISDICTION', 'BOOKINGS', 'View bookings for attractions and services within assigned jurisdiction'),
('BOOKING_VIEW_ALL', 'BOOKINGS', 'View all provincial bookings and reservations'),
('BOOKING_UPDATE_STATUS', 'BOOKINGS', 'Accept, reject, confirm, or complete booking items'),

-- Reviews & Testimonials
('REVIEW_VIEW', 'REVIEWS', 'Read traveler feedback and ratings'),
('REVIEW_CREATE', 'REVIEWS', 'Submit reviews and ratings for verified travel bookings'),
('REVIEW_MANAGE', 'REVIEWS', 'Moderate and manage published user reviews'),

-- Announcements & Bulletins
('ANNOUNCEMENT_VIEW', 'ANNOUNCEMENTS', 'Read provincial travel advisories and weather alerts'),
('ANNOUNCEMENT_PUBLISH', 'ANNOUNCEMENTS', 'Draft and broadcast provincial travel announcements'),
('ANNOUNCEMENT_DELETE', 'ANNOUNCEMENTS', 'Delete expired announcements'),

-- Reports & Analytics
('REPORT_RECORD_ARRIVAL', 'REPORTS', 'Log tourist footfall and carrying-capacity headcounts'),
('REPORT_VIEW_BARANGAY', 'REPORTS', 'Access barangay footfall records and carrying-capacity indicators'),
('REPORT_VIEW_MUNICIPALITY', 'REPORTS', 'Access municipal consolidated tourism statistics'),
('REPORT_VIEW_PROVINCIAL', 'REPORTS', 'Access province-wide arrival statistics, trends, and analytics'),

-- System Administration
('USER_MANAGE', 'ADMINISTRATION', 'Manage user accounts, assign roles, and handle account status'),
('AUDIT_VIEW', 'ADMINISTRATION', 'Inspect immutable security audit logs'),
('DATABASE_MANAGE', 'ADMINISTRATION', 'Generate database snapshots and perform maintenance')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ----------------------------------------------------------------------------
-- 5. ASSIGN PERMISSIONS TO ROLES
-- ----------------------------------------------------------------------------
-- Helper to clear and re-assign clean RBAC matrix
DELETE FROM role_permissions;

-- Role 6: TOURIST
INSERT INTO role_permissions (role_id, permission_id)
SELECT 6, permission_id FROM permissions WHERE permission_code IN (
    'DESTINATION_VIEW',
    'PACKAGE_VIEW',
    'SERVICE_VIEW',
    'STAKEHOLDER_VIEW',
    'ANNOUNCEMENT_VIEW',
    'REVIEW_VIEW',
    'BOOKING_CREATE',
    'BOOKING_VIEW_OWN',
    'REVIEW_CREATE'
);

-- Role 5: LOCAL TOURISM STAKEHOLDER
INSERT INTO role_permissions (role_id, permission_id)
SELECT 5, permission_id FROM permissions WHERE permission_code IN (
    'DESTINATION_VIEW',
    'PACKAGE_VIEW',
    'SERVICE_VIEW',
    'STAKEHOLDER_VIEW',
    'ANNOUNCEMENT_VIEW',
    'REVIEW_VIEW',
    'SERVICE_MANAGE_OWN',
    'STAKEHOLDER_MANAGE_OWN',
    'BOOKING_VIEW_OWN',
    'BOOKING_UPDATE_STATUS'
);

-- Role 4: BARANGAY OFFICIAL (Local Verifier)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, permission_id FROM permissions WHERE permission_code IN (
    'DESTINATION_VIEW',
    'PACKAGE_VIEW',
    'SERVICE_VIEW',
    'STAKEHOLDER_VIEW',
    'ANNOUNCEMENT_VIEW',
    'REVIEW_VIEW',
    'DESTINATION_VALIDATE',
    'STAKEHOLDER_VERIFY',
    'BOOKING_VIEW_JURISDICTION',
    'REPORT_RECORD_ARRIVAL',
    'REPORT_VIEW_BARANGAY'
);

-- Role 3: LGU OFFICER (Municipal Tourism Officer)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, permission_id FROM permissions WHERE permission_code IN (
    'DESTINATION_VIEW',
    'PACKAGE_VIEW',
    'SERVICE_VIEW',
    'STAKEHOLDER_VIEW',
    'ANNOUNCEMENT_VIEW',
    'REVIEW_VIEW',
    'BOOKING_VIEW_JURISDICTION',
    'REPORT_RECORD_ARRIVAL',
    'REPORT_VIEW_BARANGAY',
    'REPORT_VIEW_MUNICIPALITY'
);

-- Role 2: TOURISM OFFICE PERSONNEL (Provincial Level)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, permission_id FROM permissions WHERE permission_code IN (
    'DESTINATION_VIEW',
    'DESTINATION_CREATE',
    'DESTINATION_UPDATE',
    'DESTINATION_DELETE',
    'DESTINATION_VALIDATE',
    'PACKAGE_VIEW',
    'PACKAGE_CREATE',
    'PACKAGE_UPDATE',
    'PACKAGE_DELETE',
    'SERVICE_VIEW',
    'SERVICE_MANAGE_ALL',
    'STAKEHOLDER_VIEW',
    'BOOKING_VIEW_ALL',
    'REVIEW_VIEW',
    'REVIEW_MANAGE',
    'ANNOUNCEMENT_VIEW',
    'ANNOUNCEMENT_PUBLISH',
    'ANNOUNCEMENT_DELETE',
    'REPORT_RECORD_ARRIVAL',
    'REPORT_VIEW_BARANGAY',
    'REPORT_VIEW_MUNICIPALITY',
    'REPORT_VIEW_PROVINCIAL'
);

-- Role 1: SYSTEM ADMINISTRATOR (Universal Administrative Access)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions;
