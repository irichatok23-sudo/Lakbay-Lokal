-- ============================================================================
-- LAKBAY LOKAL: PRE-MIGRATION DATABASE BACKUP SNAPSHOT
-- Target Database: lakbay_lokal_db (MySQL 8.0+)
-- Target Location: Biliran Province, Philippines
-- Generated as safety backup before web migration
-- ============================================================================

CREATE DATABASE IF NOT EXISTS lakbay_lokal_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE lakbay_lokal_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. ROLES
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(30) NOT NULL UNIQUE,
    role_name VARCHAR(60) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB;

INSERT INTO roles (role_id, role_code, role_name, description) VALUES
(1, 'SYSTEM_ADMIN', 'System Administrator', 'Full administrative authority across all modules, roles, security, and databases.'),
(2, 'TOURISM_OFFICE', 'Tourism Office Officer', 'Province-wide promotion, announcements, arrival trend analytics, and project supervision.'),
(3, 'LGU_OFFICER', 'Local Government Unit Officer', 'Municipal-level tourism monitoring, inter-barangay coordination, and consolidated reporting.'),
(4, 'BARANGAY_OFFICIAL', 'Barangay Official', 'Local stakeholder verification, attraction validation, footfall logging, and barangay records.'),
(5, 'STAKEHOLDER', 'Local Tourism Stakeholder', 'Community guides, drivers, boat operators, cooks, and artisans managing services and bookings.'),
(6, 'TOURIST', 'Tourist', 'Visitors exploring destinations, customizing packages, booking services, and leaving reviews.')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- ----------------------------------------------------------------------------
-- 2. MUNICIPALITIES
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS municipalities (
    municipality_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    province VARCHAR(80) NOT NULL DEFAULT 'Biliran',
    zip_code VARCHAR(10)
) ENGINE=InnoDB;

INSERT INTO municipalities (municipality_id, name, province, zip_code) VALUES
(1, 'Naval', 'Biliran', '6560'),
(2, 'Almeria', 'Biliran', '6561'),
(3, 'Kawayan', 'Biliran', '6562'),
(4, 'Culaba', 'Biliran', '6563'),
(5, 'Caibiran', 'Biliran', '6564'),
(6, 'Cabucgayan', 'Biliran', '6565'),
(7, 'Biliran', 'Biliran', '6566'),
(8, 'Maripipi', 'Biliran', '6567')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ----------------------------------------------------------------------------
-- 3. BARANGAYS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS barangays (
    barangay_id INT AUTO_INCREMENT PRIMARY KEY,
    municipality_id INT NOT NULL,
    name VARCHAR(80) NOT NULL,
    contact_person VARCHAR(100),
    contact_number VARCHAR(30),
    FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_municipality_barangay (municipality_id, name)
) ENGINE=InnoDB;

INSERT INTO barangays (barangay_id, municipality_id, name, contact_person, contact_number) VALUES
(1, 8, 'Ol-og (Sambawan)', 'Hon. Ramon Corpin', '+63 917 111 2001'),
(2, 8, 'Bato', 'Hon. Elena Tan', '+63 917 111 2002'),
(3, 8, 'Ermita', 'Hon. Lucas Del Rosario', '+63 917 111 2003'),
(4, 5, 'Cabibihan (Tinago)', 'Hon. Roberto Gomez', '+63 918 222 3001'),
(5, 5, 'Manlabang', 'Hon. Corazon Santos', '+63 918 222 3002'),
(6, 2, 'Sampao (Ulan-Ulan)', 'Hon. Mario Morales', '+63 919 333 4001'),
(7, 2, 'Iyusan', 'Hon. Teresa Cruz', '+63 919 333 4002'),
(8, 2, 'Kawayanon', 'Hon. Daniel Navarro', '+63 919 333 4003'),
(9, 1, 'P.I. Garcia', 'Hon. Fernando Perez', '+63 920 444 5001'),
(10, 1, 'Smo. Rosario (Higatangan)', 'Hon. Gabriel Lim', '+63 920 444 5002'),
(11, 7, 'Busali', 'Hon. Gloria Bautista', '+63 921 555 6001'),
(12, 3, 'Baganito', 'Hon. Angela Rivera', '+63 922 666 7001')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ----------------------------------------------------------------------------
-- 4. USERS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    role_id INT NOT NULL,
    barangay_id INT NULL,
    status ENUM('ACTIVE', 'PENDING_APPROVAL', 'DEACTIVATED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

INSERT INTO users (user_id, username, password_hash, salt, email, full_name, phone, role_id, barangay_id, status) VALUES
(1, 'admin', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'admin@lakbaylokal.gov.ph', 'Administrator Maria Santos', '+63 917 000 0001', 1, 9, 'ACTIVE'),
(2, 'tourismoffice', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'tourism@biliran.gov.ph', 'Director Eduardo Reyes', '+63 917 000 0002', 2, 9, 'ACTIVE'),
(3, 'lgu_naval', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'lgu.naval@biliran.gov.ph', 'Officer Katrina Gonzales', '+63 917 000 0003', 3, 9, 'ACTIVE'),
(4, 'brgy_olog', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'brgy.olog@maripipi.gov.ph', 'Captain Ramon Corpin', '+63 917 111 2001', 4, 1, 'ACTIVE'),
(5, 'guide_pedro', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'pedro.guide@gmail.com', 'Pedro Penduko', '+63 928 111 8888', 5, 1, 'ACTIVE'),
(6, 'driver_juan', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'juan.transpo@gmail.com', 'Juan Dela Cruz', '+63 928 222 9999', 5, 6, 'ACTIVE'),
(7, 'boat_capitan', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'maripipi.boats@gmail.com', 'Capitan Danilo Flores', '+63 928 333 7777', 5, 1, 'ACTIVE'),
(8, 'cook_nanay_luz', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'nanayluz.catering@gmail.com', 'Luzviminda Ocampo', '+63 928 444 6666', 5, 4, 'ACTIVE'),
(9, 'artisan_rosa', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'maripipi.pottery@gmail.com', 'Rosa Mangahas', '+63 928 555 5555', 5, 2, 'ACTIVE'),
(10, 'tourist_ana', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'ana.tourist@gmail.com', 'Ana Sophia Cortez', '+63 915 777 3333', 6, NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), salt = VALUES(salt), full_name = VALUES(full_name);

SET FOREIGN_KEY_CHECKS = 1;
