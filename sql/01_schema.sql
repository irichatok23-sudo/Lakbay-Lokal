

CREATE DATABASE IF NOT EXISTS lakbay_lokal_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE lakbay_lokal_db;


SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(30) NOT NULL UNIQUE,
    role_name VARCHAR(60) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS municipalities;
CREATE TABLE municipalities (
    municipality_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    province VARCHAR(80) NOT NULL DEFAULT 'Biliran',
    zip_code VARCHAR(10)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS barangays;
CREATE TABLE barangays (
    barangay_id INT AUTO_INCREMENT PRIMARY KEY,
    municipality_id INT NOT NULL,
    name VARCHAR(80) NOT NULL,
    contact_person VARCHAR(100),
    contact_number VARCHAR(30),
    FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_municipality_barangay (municipality_id, name)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    role_id INT NOT NULL,
    barangay_id INT NULL,
    municipality_id INT NULL,
    status ENUM('ACTIVE', 'PENDING_APPROVAL', 'DEACTIVATED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS tourist_profiles;
CREATE TABLE tourist_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    origin_city VARCHAR(100),
    nationality VARCHAR(60) DEFAULT 'Filipino',
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(30),
    travel_preferences TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB

DROP TABLE IF EXISTS stakeholder_profiles;
CREATE TABLE stakeholder_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    barangay_id INT NOT NULL,
    business_name VARCHAR(120) NOT NULL,
    category ENUM('TOUR_GUIDE', 'TRANSPORT_DRIVER', 'BOAT_OPERATOR', 'LOCAL_COOK', 'ARTISAN') NOT NULL,
    accreditation_no VARCHAR(60),
    contact_number VARCHAR(30),
    years_experience INT DEFAULT 1,
    verification_status ENUM('PENDING', 'UNDER_REVIEW', 'NEEDS_CORRECTION', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    verified_by INT NULL,
    verified_at DATETIME NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS tourism_services;
CREATE TABLE tourism_services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    stakeholder_id INT NOT NULL,
    service_name VARCHAR(120) NOT NULL,
    service_category ENUM('GUIDING', 'TRANSPORT', 'BOAT_CHARTER', 'FOOD_CATERING', 'HANDICRAFT') NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    pricing_unit ENUM('PER_PERSON', 'PER_GROUP', 'PER_TRIP', 'PER_ITEM') NOT NULL DEFAULT 'PER_PERSON',
    max_capacity INT NOT NULL DEFAULT 1,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stakeholder_id) REFERENCES stakeholder_profiles(profile_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS destinations;
CREATE TABLE destinations (
    destination_id INT AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NOT NULL,
    name VARCHAR(120) NOT NULL,
    category ENUM('WATERFALL', 'ISLAND_BEACH', 'HOT_SPRING', 'CULTURAL_HERITAGE', 'CAVE_MOUNTAIN') NOT NULL,
    description TEXT NOT NULL,
    entrance_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    operating_hours VARCHAR(100) DEFAULT '6:00 AM - 5:00 PM',
    location_coordinates VARCHAR(80),
    contact_info VARCHAR(120),
    image_url VARCHAR(255),
    status ENUM('ACTIVE', 'UNDER_MAINTENANCE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    validated_by INT NULL,
    validated_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (validated_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS tourism_packages;
CREATE TABLE tourism_packages (
    package_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    description TEXT NOT NULL,
    package_type ENUM('PRE_DESIGNED', 'CUSTOM') NOT NULL DEFAULT 'PRE_DESIGNED',
    price_per_pax DECIMAL(10, 2) NOT NULL,
    duration_days INT NOT NULL DEFAULT 1,
    min_pax INT NOT NULL DEFAULT 1,
    max_pax INT NOT NULL DEFAULT 15,
    inclusions TEXT,
    exclusions TEXT,
    status ENUM('ACTIVE', 'DRAFT', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    image_url VARCHAR(255),
    created_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS package_destinations;
CREATE TABLE package_destinations (
    package_id INT NOT NULL,
    destination_id INT NOT NULL,
    visit_order INT NOT NULL DEFAULT 1,
    PRIMARY KEY (package_id, destination_id),
    FOREIGN KEY (package_id) REFERENCES tourism_packages(package_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS package_services;
CREATE TABLE package_services (
    package_id INT NOT NULL,
    service_id INT NOT NULL,
    PRIMARY KEY (package_id, service_id),
    FOREIGN KEY (package_id) REFERENCES tourism_packages(package_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (service_id) REFERENCES tourism_services(service_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS bookings;
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(30) NOT NULL UNIQUE,
    tourist_id INT NOT NULL,
    package_id INT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    travel_date_start DATE NOT NULL,
    travel_date_end DATE NOT NULL,
    number_of_pax INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    custom_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tourist_id) REFERENCES tourist_profiles(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (package_id) REFERENCES tourism_packages(package_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS booking_service_items;
CREATE TABLE booking_service_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    service_id INT NOT NULL,
    stakeholder_id INT NOT NULL,
    scheduled_time DATETIME NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    subtotal DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (service_id) REFERENCES tourism_services(service_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (stakeholder_id) REFERENCES stakeholder_profiles(profile_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    tourist_id INT NOT NULL,
    target_type ENUM('PACKAGE', 'STAKEHOLDER', 'DESTINATION') NOT NULL,
    target_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (tourist_id) REFERENCES tourist_profiles(profile_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS verification_records;
CREATE TABLE verification_records (
    verification_id INT AUTO_INCREMENT PRIMARY KEY,
    stakeholder_id INT NOT NULL,
    barangay_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'REQUEST_CORRECTION') NOT NULL,
    requirements_submitted TEXT,
    remarks TEXT,
    decision_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stakeholder_id) REFERENCES stakeholder_profiles(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS announcements;
CREATE TABLE announcements (
    announcement_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    category ENUM('ANNOUNCEMENT', 'EVENT', 'FESTIVAL', 'TRAVEL_GUIDELINE') NOT NULL,
    target_barangay_id INT NULL,
    published_by INT NOT NULL,
    publish_date DATE NOT NULL,
    expiry_date DATE NULL,
    status ENUM('PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_barangay_id) REFERENCES barangays(barangay_id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (published_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS tourism_arrival_logs;
CREATE TABLE tourism_arrival_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    destination_id INT NOT NULL,
    log_date DATE NOT NULL,
    local_tourists_count INT NOT NULL DEFAULT 0,
    domestic_tourists_count INT NOT NULL DEFAULT 0,
    foreign_tourists_count INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    recorded_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destination_id) REFERENCES destinations(destination_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS audit_logs;
CREATE TABLE audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_name VARCHAR(60) NOT NULL,
    entity_id INT NULL,
    ip_address VARCHAR(50) DEFAULT '127.0.0.1',
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;


SET FOREIGN_KEY_CHECKS = 1;


CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_destinations_barangay ON destinations(barangay_id);
CREATE INDEX idx_destinations_category ON destinations(category);
CREATE INDEX idx_bookings_tourist ON bookings(tourist_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_dates ON bookings(travel_date_start, travel_date_end);
CREATE INDEX idx_stakeholders_barangay ON stakeholder_profiles(barangay_id);
CREATE INDEX idx_stakeholders_status ON stakeholder_profiles(verification_status);
CREATE INDEX idx_arrival_logs_dest_date ON tourism_arrival_logs(destination_id, log_date);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
