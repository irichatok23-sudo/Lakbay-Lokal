-- MySQL dump 10.13  Distrib 8.4.7, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: lakbay_lokal_db
-- ------------------------------------------------------
-- Server version	8.4.7

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `announcement_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` enum('ANNOUNCEMENT','EVENT','FESTIVAL','TRAVEL_GUIDELINE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_barangay_id` int DEFAULT NULL,
  `published_by` int NOT NULL,
  `publish_date` date NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `status` enum('PUBLISHED','ARCHIVED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PUBLISHED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`announcement_id`),
  KEY `target_barangay_id` (`target_barangay_id`),
  KEY `published_by` (`published_by`),
  CONSTRAINT `announcements_ibfk_1` FOREIGN KEY (`target_barangay_id`) REFERENCES `barangays` (`barangay_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `announcements_ibfk_2` FOREIGN KEY (`published_by`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES (1,'Biliran Province 34th Foundation Anniversary & Tourism Expo','Join us from May 7 to 11 in Naval for the annual Province-Wide Tourism Expo, featuring local culinary showcases, artisanal handicrafts, and eco-tour discounts.','FESTIVAL',NULL,2,'2026-04-01','2026-05-15','PUBLISHED','2026-11-11 11:43:26'),(2,'Sustainable Eco-Trekking Guidelines for Ulan-Ulan and Tinago Falls','All visitors are reminded to observe Leave No Trace principles: single-use plastics are prohibited within 50 meters of water basins. Certified community guides are mandatory.','TRAVEL_GUIDELINE',6,2,'2026-02-01','2026-12-31','PUBLISHED','2026-11-11 11:43:26'),(3,'Coast Guard Sea Travel Advisory: Calm Seas at Maripipi & Sambawan','Philippine Coast Guard reports favorable sea conditions across the Biliran Strait. Motorized tourist vessels from Kawayan and Naval are cleared for regular island crossings.','ANNOUNCEMENT',1,2,'2026-03-01','2026-04-30','PUBLISHED','2026-11-11 11:43:26');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_name` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_id` int DEFAULT NULL,
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '127.0.0.1',
  `details` text COLLATE utf8mb4_unicode_ci,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  KEY `idx_audit_logs_timestamp` (`timestamp`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,1,'admin','SYSTEM_INITIALIZATION','DATABASE',1,'127.0.0.1','Lakbay Lokal database initialized with seed data for Biliran Province.','2025-12-31 16:00:00'),(2,4,'brgy_olog','STAKEHOLDER_VERIFIED','STAKEHOLDER_PROFILE',1,'127.0.0.1','Barangay Captain Ramon Corpin verified Sambawan Island Eco-Trek Guides.','2026-01-15 01:30:00'),(3,10,'tourist_ana','CREATE_BOOKING','BOOKINGS',1,'127.0.0.1','Created booking reference LL-2026-0001 for Sambawan Island Paradise Escape.','2026-02-15 02:00:00'),(4,5,'guide_pedro','LOGIN_SUCCESS','AUTH',5,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 11:48:46'),(5,1,'admin','LOGIN_SUCCESS','AUTH',1,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 11:59:10'),(6,1,'admin','LOGIN_SUCCESS','AUTH',1,'127.0.0.1','User logged in','2026-11-11 12:29:08'),(7,1,'admin','LOGIN_SUCCESS','AUTH',1,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:34:48'),(8,10,'tourist_ana','LOGIN_SUCCESS','AUTH',10,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:36:30'),(9,NULL,'lexey','REGISTER_TOURIST','USERS',NULL,'0:0:0:0:0:0:0:1','New tourist registered: Alexa Romania','2026-11-11 12:38:25'),(10,11,'lexey','LOGIN_SUCCESS','AUTH',11,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:38:37'),(11,2,'tourismoffice','LOGIN_SUCCESS','AUTH',2,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:41:47'),(12,3,'lgu_naval','LOGIN_SUCCESS','AUTH',3,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:42:20'),(13,5,'guide_pedro','LOGIN_SUCCESS','AUTH',5,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:43:37'),(14,2,'tourismoffice','LOGIN_SUCCESS','AUTH',2,'0:0:0:0:0:0:0:1','User logged in','2026-11-11 12:44:45'),(15,1,'admin','LOGIN_SUCCESS','AUTH',1,'127.0.0.1','User logged in','2026-11-11 12:53:17'),(16,2,'tourismoffice','LOGIN_SUCCESS','AUTH',2,'127.0.0.1','User logged in','2026-11-11 12:53:17'),(17,3,'lgu_naval','LOGIN_SUCCESS','AUTH',3,'127.0.0.1','User logged in','2026-11-11 12:53:17'),(18,4,'brgy_olog','LOGIN_SUCCESS','AUTH',4,'127.0.0.1','User logged in','2026-11-11 12:53:17'),(19,5,'guide_pedro','LOGIN_SUCCESS','AUTH',5,'127.0.0.1','User logged in','2026-11-11 12:53:17'),(20,10,'tourist_ana','LOGIN_SUCCESS','AUTH',10,'127.0.0.1','User logged in','2026-11-11 12:53:17'),(22,1,'admin','UPDATE_USER_STATUS','USERS',12,'127.0.0.1','Set status to: DEACTIVATED','2026-11-11 12:53:17'),(23,1,'admin','UPDATE_USER_STATUS','USERS',12,'127.0.0.1','Set status to: ACTIVE','2026-11-11 12:53:18'),(24,1,'admin','RESET_PASSWORD','USERS',12,'127.0.0.1','Password reset by admin','2026-11-11 12:53:18'),(28,4,'brgy_olog','VALIDATE_DESTINATION','DESTINATIONS',9,'127.0.0.1','Validated by Captain Ramon Corpin','2026-11-11 12:53:18'),(29,2,'tourismoffice','DELETE_DESTINATION','DESTINATIONS',9,'127.0.0.1','Deleted destination ID: 9','2026-11-11 12:53:18'),(32,2,'tourismoffice','DELETE_ANNOUNCEMENT','ANNOUNCEMENTS',4,'127.0.0.1','Deleted announcement ID: 4','2026-11-11 12:53:18'),(35,2,'tourismoffice','DELETE_PACKAGE','PACKAGES',5,'127.0.0.1','Deleted package ID: 5','2026-11-11 12:53:18'),(39,5,'guide_pedro','DELETE_SERVICE','SERVICES',10,'127.0.0.1','Deleted service ID: 10','2026-11-11 12:53:19'),(40,10,'tourist_ana','CREATE_BOOKING','BOOKINGS',4,'127.0.0.1','New booking created: LL-2026-8794','2026-11-11 12:53:19'),(41,10,'tourist_ana','CREATE_BOOKING','BOOKINGS',5,'127.0.0.1','New booking created: LL-2026-4312','2026-11-11 12:53:19'),(42,5,'guide_pedro','UPDATE_BOOKING_STATUS','BOOKINGS',5,'127.0.0.1','Booking status set to ACCEPTED','2026-11-11 12:53:19'),(43,5,'guide_pedro','UPDATE_BOOKING_STATUS','BOOKINGS',5,'127.0.0.1','Booking status set to COMPLETED','2026-11-11 12:53:19'),(44,10,'tourist_ana','CREATE_REVIEW','REVIEWS',4,'127.0.0.1','Rating: 5 stars','2026-11-11 12:53:19'),(45,3,'lgu_naval','LOG_ARRIVAL','ARRIVAL_LOGS',6,'127.0.0.1','Visitors recorded for destination ID: 1','2026-11-11 12:53:19');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `barangays`
--

DROP TABLE IF EXISTS `barangays`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `barangays` (
  `barangay_id` int NOT NULL AUTO_INCREMENT,
  `municipality_id` int NOT NULL,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_person` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_number` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`barangay_id`),
  UNIQUE KEY `uq_municipality_barangay` (`municipality_id`,`name`),
  CONSTRAINT `barangays_ibfk_1` FOREIGN KEY (`municipality_id`) REFERENCES `municipalities` (`municipality_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `barangays`
--

LOCK TABLES `barangays` WRITE;
/*!40000 ALTER TABLE `barangays` DISABLE KEYS */;
INSERT INTO `barangays` VALUES (1,8,'Ol-og (Sambawan)','Hon. Ramon Corpin','+63 917 111 2001'),(2,8,'Bato','Hon. Elena Tan','+63 917 111 2002'),(3,8,'Ermita','Hon. Lucas Del Rosario','+63 917 111 2003'),(4,5,'Cabibihan (Tinago)','Hon. Roberto Gomez','+63 918 222 3001'),(5,5,'Manlabang','Hon. Corazon Santos','+63 918 222 3002'),(6,2,'Sampao (Ulan-Ulan)','Hon. Mario Morales','+63 919 333 4001'),(7,2,'Iyusan','Hon. Teresa Cruz','+63 919 333 4002'),(8,2,'Kawayanon','Hon. Daniel Navarro','+63 919 333 4003'),(9,1,'P.I. Garcia','Hon. Fernando Perez','+63 920 444 5001'),(10,1,'Smo. Rosario (Higatangan)','Hon. Gabriel Lim','+63 920 444 5002'),(11,7,'Busali','Hon. Gloria Bautista','+63 921 555 6001'),(12,3,'Baganito','Hon. Angela Rivera','+63 922 666 7001');
/*!40000 ALTER TABLE `barangays` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_service_items`
--

DROP TABLE IF EXISTS `booking_service_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_service_items` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `service_id` int NOT NULL,
  `stakeholder_id` int NOT NULL,
  `scheduled_time` datetime DEFAULT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `subtotal` decimal(10,2) NOT NULL,
  `status` enum('PENDING','ACCEPTED','REJECTED','COMPLETED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`item_id`),
  KEY `booking_id` (`booking_id`),
  KEY `service_id` (`service_id`),
  KEY `stakeholder_id` (`stakeholder_id`),
  CONSTRAINT `booking_service_items_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `booking_service_items_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `tourism_services` (`service_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `booking_service_items_ibfk_3` FOREIGN KEY (`stakeholder_id`) REFERENCES `stakeholder_profiles` (`profile_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_service_items`
--

LOCK TABLES `booking_service_items` WRITE;
/*!40000 ALTER TABLE `booking_service_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_service_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `booking_reference` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tourist_id` int NOT NULL,
  `package_id` int DEFAULT NULL,
  `booking_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `travel_date_start` date NOT NULL,
  `travel_date_end` date NOT NULL,
  `number_of_pax` int NOT NULL DEFAULT '1',
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('PENDING','ACCEPTED','CONFIRMED','COMPLETED','CANCELLED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `custom_notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `booking_reference` (`booking_reference`),
  KEY `package_id` (`package_id`),
  KEY `idx_bookings_tourist` (`tourist_id`),
  KEY `idx_bookings_status` (`status`),
  KEY `idx_bookings_dates` (`travel_date_start`,`travel_date_end`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`tourist_id`) REFERENCES `tourist_profiles` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`package_id`) REFERENCES `tourism_packages` (`package_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,'LL-2026-0001',1,1,'2026-02-15 02:00:00','2026-03-20','2026-03-21',4,7400.00,'COMPLETED','Group of 4 photography enthusiasts, requesting sunset trek at Sambawan hill.','2026-11-11 11:43:26','2026-11-11 11:43:26'),(2,'LL-2026-0002',1,2,'2026-03-01 06:30:00','2026-04-10','2026-04-10',2,2500.00,'CONFIRMED','Vegetarian lunch request for one guest.','2026-11-11 11:43:26','2026-11-11 11:43:26'),(3,'LL-2026-0003',1,NULL,'2026-03-05 01:15:00','2026-04-18','2026-04-18',3,2100.00,'PENDING','Custom booking: Habal-habal tour plus Pottery workshop.','2026-11-11 11:43:26','2026-11-11 11:43:26');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `destinations`
--

DROP TABLE IF EXISTS `destinations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `destinations` (
  `destination_id` int NOT NULL AUTO_INCREMENT,
  `barangay_id` int NOT NULL,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` enum('WATERFALL','ISLAND_BEACH','HOT_SPRING','CULTURAL_HERITAGE','CAVE_MOUNTAIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `entrance_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `operating_hours` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '6:00 AM - 5:00 PM',
  `location_coordinates` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_info` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','UNDER_MAINTENANCE','CLOSED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `validated_by` int DEFAULT NULL,
  `validated_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`destination_id`),
  KEY `validated_by` (`validated_by`),
  KEY `idx_destinations_barangay` (`barangay_id`),
  KEY `idx_destinations_category` (`category`),
  CONSTRAINT `destinations_ibfk_1` FOREIGN KEY (`barangay_id`) REFERENCES `barangays` (`barangay_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `destinations_ibfk_2` FOREIGN KEY (`validated_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `destinations`
--

LOCK TABLES `destinations` WRITE;
/*!40000 ALTER TABLE `destinations` DISABLE KEYS */;
INSERT INTO `destinations` VALUES (1,1,'Sambawan Island Marine Sanctuary','ISLAND_BEACH','A world-class semi-curved island paradise featuring rocky volcanic peaks, pristine white sand beaches, rich coral marine sanctuary, and a scenic observation tower.',120.00,'Open 24 Hours (Overnight Camping Available)','11.6025, 124.2628','maripipi.tourism@biliran.gov.ph','sambawan_island.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(2,4,'Tinago Falls Eco-Park','WATERFALL','A majestic waterfall gracefully cascading into a deep, turquoise blue natural pool framed by lush jungle foliage and wooden picnic cottages.',50.00,'7:00 AM - 5:00 PM','11.5367, 124.5244','tinago.caibiran@biliran.gov.ph','tinago_falls.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(3,6,'Ulan-Ulan Falls','WATERFALL','A 25-meter roaring waterfall whose spray resembles a gentle rain shower (ulan), nestled in a tropical ravine perfect for nature enthusiasts.',40.00,'6:30 AM - 4:30 PM','11.5839, 124.4172','almeria.tourism@biliran.gov.ph','ulan_ulan_falls.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(4,10,'Higatangan Island & Shifting Sandbar','ISLAND_BEACH','An island marvel featuring a 200-meter white sandbar that magically shifts direction with ocean tides, clear snorkeling waters, and volcanic rock formations.',80.00,'6:00 AM - 6:00 PM','11.5647, 124.2811','naval.tourism@biliran.gov.ph','higatangan_sandbar.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(5,5,'Mainit Hot Spring','HOT_SPRING','Natural therapeutic mineral hot springs flowing down a geothermal river bed with temperature pools designed for relaxation and healing.',40.00,'7:00 AM - 7:00 PM','11.5511, 124.5422','caibiran.hotspring@biliran.gov.ph','mainit_spring.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(6,7,'Iyusan Rice Terraces','CULTURAL_HERITAGE','Spectacular centuries-old stone-walled rice terraces carved onto verdant mountain slopes reflecting community agricultural heritage.',25.00,'6:00 AM - 5:30 PM','11.5714, 124.4022','iyusan.brgy@almeria.gov.ph','iyusan_terraces.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(7,2,'Maripipi Pottery & Cultural Village','CULTURAL_HERITAGE','Traditional community hub where generational pottery artisans mold natural red clay pots (daba) and clay ovens using indigenous techniques.',30.00,'8:00 AM - 4:00 PM','11.7708, 124.3411','maripipi.artisan@biliran.gov.ph','maripipi_pottery.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26'),(8,6,'Reco Falls','WATERFALL','A serene and tranquil swimming hole located upstream from Ulan-Ulan falls, offering calm emerald waters and natural diving ledges.',30.00,'7:00 AM - 4:30 PM','11.5894, 124.4219','almeria.tourism@biliran.gov.ph','reco_falls.jpg','ACTIVE',1,'2026-01-10 10:00:00','2026-11-11 11:43:26');
/*!40000 ALTER TABLE `destinations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `municipalities`
--

DROP TABLE IF EXISTS `municipalities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `municipalities` (
  `municipality_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Biliran',
  `zip_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`municipality_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `municipalities`
--

LOCK TABLES `municipalities` WRITE;
/*!40000 ALTER TABLE `municipalities` DISABLE KEYS */;
INSERT INTO `municipalities` VALUES (1,'Naval','Biliran','6560'),(2,'Almeria','Biliran','6561'),(3,'Kawayan','Biliran','6562'),(4,'Culaba','Biliran','6563'),(5,'Caibiran','Biliran','6564'),(6,'Cabucgayan','Biliran','6565'),(7,'Biliran','Biliran','6566'),(8,'Maripipi','Biliran','6567');
/*!40000 ALTER TABLE `municipalities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `package_destinations`
--

DROP TABLE IF EXISTS `package_destinations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `package_destinations` (
  `package_id` int NOT NULL,
  `destination_id` int NOT NULL,
  `visit_order` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`package_id`,`destination_id`),
  KEY `destination_id` (`destination_id`),
  CONSTRAINT `package_destinations_ibfk_1` FOREIGN KEY (`package_id`) REFERENCES `tourism_packages` (`package_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `package_destinations_ibfk_2` FOREIGN KEY (`destination_id`) REFERENCES `destinations` (`destination_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `package_destinations`
--

LOCK TABLES `package_destinations` WRITE;
/*!40000 ALTER TABLE `package_destinations` DISABLE KEYS */;
INSERT INTO `package_destinations` VALUES (1,1,1),(2,2,1),(2,3,2),(2,8,3),(3,4,1),(4,7,1);
/*!40000 ALTER TABLE `package_destinations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `package_services`
--

DROP TABLE IF EXISTS `package_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `package_services` (
  `package_id` int NOT NULL,
  `service_id` int NOT NULL,
  PRIMARY KEY (`package_id`,`service_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `package_services_ibfk_1` FOREIGN KEY (`package_id`) REFERENCES `tourism_packages` (`package_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `package_services_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `tourism_services` (`service_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `package_services`
--

LOCK TABLES `package_services` WRITE;
/*!40000 ALTER TABLE `package_services` DISABLE KEYS */;
INSERT INTO `package_services` VALUES (1,1),(2,2),(2,3),(1,5),(3,6),(1,7),(4,9);
/*!40000 ALTER TABLE `package_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `permission_id` int NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `permission_code` (`permission_code`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES (1,'DESTINATION_VIEW','DESTINATIONS','View published eco-destinations and tourist spots'),(2,'DESTINATION_CREATE','DESTINATIONS','Register and publish new tourism destinations in Biliran'),(3,'DESTINATION_UPDATE','DESTINATIONS','Modify destination details, entrance fees, and descriptions'),(4,'DESTINATION_DELETE','DESTINATIONS','Remove or archive destination records'),(5,'DESTINATION_VALIDATE','DESTINATIONS','Validate and endorse destination within authorized jurisdiction'),(6,'PACKAGE_VIEW','PACKAGES','Browse community-curated tour circuits and packages'),(7,'PACKAGE_CREATE','PACKAGES','Create new community tourism packages and itinerary circuits'),(8,'PACKAGE_UPDATE','PACKAGES','Edit package pricing, itinerary inclusions, and durations'),(9,'PACKAGE_DELETE','PACKAGES','Remove or archive community travel packages'),(10,'SERVICE_VIEW','SERVICES','View accredited stakeholder rate cards and services'),(11,'SERVICE_MANAGE_OWN','SERVICES','Manage, create, and update own stakeholder service offerings'),(12,'SERVICE_MANAGE_ALL','SERVICES','Manage, edit, or adjust all stakeholder services province-wide'),(13,'STAKEHOLDER_VIEW','STAKEHOLDERS','Access verified stakeholder registry and contact details'),(14,'STAKEHOLDER_MANAGE_OWN','STAKEHOLDERS','Manage personal stakeholder business profile'),(15,'STAKEHOLDER_VERIFY','STAKEHOLDERS','Conduct verification, inspect documents, and endorse local stakeholders'),(16,'BOOKING_CREATE','BOOKINGS','Submit custom and pre-designed travel bookings'),(17,'BOOKING_VIEW_OWN','BOOKINGS','View own tourist reservations or stakeholder assigned bookings'),(18,'BOOKING_VIEW_JURISDICTION','BOOKINGS','View bookings for attractions and services within assigned jurisdiction'),(19,'BOOKING_VIEW_ALL','BOOKINGS','View all provincial bookings and reservations'),(20,'BOOKING_UPDATE_STATUS','BOOKINGS','Accept, reject, confirm, or complete booking items'),(21,'REVIEW_VIEW','REVIEWS','Read traveler feedback and ratings'),(22,'REVIEW_CREATE','REVIEWS','Submit reviews and ratings for verified travel bookings'),(23,'REVIEW_MANAGE','REVIEWS','Moderate and manage published user reviews'),(24,'ANNOUNCEMENT_VIEW','ANNOUNCEMENTS','Read provincial travel advisories and weather alerts'),(25,'ANNOUNCEMENT_PUBLISH','ANNOUNCEMENTS','Draft and broadcast provincial travel announcements'),(26,'ANNOUNCEMENT_DELETE','ANNOUNCEMENTS','Delete expired announcements'),(27,'REPORT_RECORD_ARRIVAL','REPORTS','Log tourist footfall and carrying-capacity headcounts'),(28,'REPORT_VIEW_BARANGAY','REPORTS','Access barangay footfall records and carrying-capacity indicators'),(29,'REPORT_VIEW_MUNICIPALITY','REPORTS','Access municipal consolidated tourism statistics'),(30,'REPORT_VIEW_PROVINCIAL','REPORTS','Access province-wide arrival statistics, trends, and analytics'),(31,'USER_MANAGE','ADMINISTRATION','Manage user accounts, assign roles, and handle account status'),(32,'AUDIT_VIEW','ADMINISTRATION','Inspect immutable security audit logs'),(33,'DATABASE_MANAGE','ADMINISTRATION','Generate database snapshots and perform maintenance');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `tourist_id` int NOT NULL,
  `target_type` enum('PACKAGE','STAKEHOLDER','DESTINATION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  KEY `booking_id` (`booking_id`),
  KEY `tourist_id` (`tourist_id`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`tourist_id`) REFERENCES `tourist_profiles` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,1,1,'PACKAGE',1,5,'Sambawan Island is sheer paradise! The boat ride was smooth, our guide Kuya Pedro took magnificent photos, and the seafood boodle fight was delicious!','2026-03-22 00:30:00'),(2,1,1,'STAKEHOLDER',1,5,'Kuya Pedro is very knowledgeable about the coral reefs and ensured all of us wore life jackets while snorkeling. Truly community-based excellence!','2026-03-22 00:35:00'),(3,1,1,'DESTINATION',1,5,'The view from the Sambawan hill at sunrise took our breath away. Clean facilities and well-maintained trail.','2026-03-22 00:40:00');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`permission_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */;
INSERT INTO `role_permissions` VALUES (1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(1,2),(2,2),(1,3),(2,3),(1,4),(2,4),(1,5),(2,5),(4,5),(1,6),(2,6),(3,6),(4,6),(5,6),(6,6),(1,7),(2,7),(1,8),(2,8),(1,9),(2,9),(1,10),(2,10),(3,10),(4,10),(5,10),(6,10),(1,11),(5,11),(1,12),(2,12),(1,13),(2,13),(3,13),(4,13),(5,13),(6,13),(1,14),(5,14),(1,15),(4,15),(1,16),(6,16),(1,17),(5,17),(6,17),(1,18),(3,18),(4,18),(1,19),(2,19),(1,20),(5,20),(1,21),(2,21),(3,21),(4,21),(5,21),(6,21),(1,22),(6,22),(1,23),(2,23),(1,24),(2,24),(3,24),(4,24),(5,24),(6,24),(1,25),(2,25),(1,26),(2,26),(1,27),(2,27),(3,27),(4,27),(1,28),(2,28),(3,28),(4,28),(1,29),(2,29),(3,29),(1,30),(2,30),(1,31),(1,32),(1,33);
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'SYSTEM_ADMIN','System Administrator','Full administrative authority across all modules, roles, security, and databases.'),(2,'TOURISM_OFFICE','Tourism Office Officer','Province-wide promotion, announcements, arrival trend analytics, and project supervision.'),(3,'LGU_OFFICER','Local Government Unit Officer','Municipal-level tourism monitoring, inter-barangay coordination, and consolidated reporting.'),(4,'BARANGAY_OFFICIAL','Barangay Official','Local stakeholder verification, attraction validation, footfall logging, and barangay records.'),(5,'STAKEHOLDER','Local Tourism Stakeholder','Community guides, drivers, boat operators, cooks, and artisans managing services and bookings.'),(6,'TOURIST','Tourist','Visitors exploring destinations, customizing packages, booking services, and leaving reviews.');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stakeholder_profiles`
--

DROP TABLE IF EXISTS `stakeholder_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stakeholder_profiles` (
  `profile_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `barangay_id` int NOT NULL,
  `business_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` enum('TOUR_GUIDE','TRANSPORT_DRIVER','BOAT_OPERATOR','LOCAL_COOK','ARTISAN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `accreditation_no` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_number` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `years_experience` int DEFAULT '1',
  `verification_status` enum('PENDING','VERIFIED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `verified_by` int DEFAULT NULL,
  `verified_at` datetime DEFAULT NULL,
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `verified_by` (`verified_by`),
  KEY `idx_stakeholders_barangay` (`barangay_id`),
  KEY `idx_stakeholders_status` (`verification_status`),
  CONSTRAINT `stakeholder_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `stakeholder_profiles_ibfk_2` FOREIGN KEY (`barangay_id`) REFERENCES `barangays` (`barangay_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `stakeholder_profiles_ibfk_3` FOREIGN KEY (`verified_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stakeholder_profiles`
--

LOCK TABLES `stakeholder_profiles` WRITE;
/*!40000 ALTER TABLE `stakeholder_profiles` DISABLE KEYS */;
INSERT INTO `stakeholder_profiles` VALUES (1,5,1,'Sambawan Island Eco-Trek & Marine Guides','TOUR_GUIDE','DOT-R8-TG-2024-001','+63 928 111 8888',6,'VERIFIED',4,'2026-01-15 09:30:00'),(2,6,6,'Almeria Habal-Habal & Van Adventure Transport','TRANSPORT_DRIVER','LGU-ALM-TR-2025-014','+63 928 222 9999',4,'VERIFIED',4,'2026-02-10 14:00:00'),(3,7,1,'Maripipi Seafarers Motorized Pumpboat Cooperative','BOAT_OPERATOR','MARINA-EV-2024-099','+63 928 333 7777',10,'VERIFIED',4,'2026-01-20 11:15:00'),(4,8,4,'Nanay Luz Traditional Biliran Kakanin & Beach Feasts','LOCAL_COOK','BRGY-CAB-LK-2025-003','+63 928 444 6666',8,'VERIFIED',4,'2026-02-05 10:00:00'),(5,9,2,'Maripipi Heritage Clay Pottery & Woven Handicrafts','ARTISAN','DTI-BIL-ART-2024-042','+63 928 555 5555',12,'VERIFIED',4,'2026-01-18 16:45:00');
/*!40000 ALTER TABLE `stakeholder_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tourism_arrival_logs`
--

DROP TABLE IF EXISTS `tourism_arrival_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tourism_arrival_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `destination_id` int NOT NULL,
  `log_date` date NOT NULL,
  `local_tourists_count` int NOT NULL DEFAULT '0',
  `domestic_tourists_count` int NOT NULL DEFAULT '0',
  `foreign_tourists_count` int NOT NULL DEFAULT '0',
  `total_revenue` decimal(10,2) NOT NULL DEFAULT '0.00',
  `recorded_by` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `recorded_by` (`recorded_by`),
  KEY `idx_arrival_logs_dest_date` (`destination_id`,`log_date`),
  CONSTRAINT `tourism_arrival_logs_ibfk_1` FOREIGN KEY (`destination_id`) REFERENCES `destinations` (`destination_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `tourism_arrival_logs_ibfk_2` FOREIGN KEY (`recorded_by`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tourism_arrival_logs`
--

LOCK TABLES `tourism_arrival_logs` WRITE;
/*!40000 ALTER TABLE `tourism_arrival_logs` DISABLE KEYS */;
INSERT INTO `tourism_arrival_logs` VALUES (1,1,'2026-03-01',45,120,18,21960.00,4,'2026-11-11 11:43:26'),(2,1,'2026-03-02',30,85,12,15240.00,4,'2026-11-11 11:43:26'),(3,2,'2026-03-01',60,140,5,10250.00,1,'2026-11-11 11:43:26'),(4,3,'2026-03-01',25,90,8,4920.00,1,'2026-11-11 11:43:26'),(5,4,'2026-03-01',20,65,4,7120.00,1,'2026-11-11 11:43:26');
/*!40000 ALTER TABLE `tourism_arrival_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tourism_packages`
--

DROP TABLE IF EXISTS `tourism_packages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tourism_packages` (
  `package_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(140) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `package_type` enum('PRE_DESIGNED','CUSTOM') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRE_DESIGNED',
  `price_per_pax` decimal(10,2) NOT NULL,
  `duration_days` int NOT NULL DEFAULT '1',
  `min_pax` int NOT NULL DEFAULT '1',
  `max_pax` int NOT NULL DEFAULT '15',
  `inclusions` text COLLATE utf8mb4_unicode_ci,
  `exclusions` text COLLATE utf8mb4_unicode_ci,
  `status` enum('ACTIVE','DRAFT','ARCHIVED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`package_id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `tourism_packages_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tourism_packages`
--

LOCK TABLES `tourism_packages` WRITE;
/*!40000 ALTER TABLE `tourism_packages` DISABLE KEYS */;
INSERT INTO `tourism_packages` VALUES (1,'Sambawan Island Marine Paradise Escape','Experience Biliran’s premier marine sanctuary with roundtrip boat charter from Kawayan, hilltop viewpoint trek, snorkeling guide, and beach boodle feast.','PRE_DESIGNED',1850.00,2,4,10,'Boat charter, entrance fees, certified guide, camping tent/cottage, seafood lunch feast','Personal snacks, snorkeling gear rental','ACTIVE','package_sambawan.jpg',1,'2026-11-11 11:43:26'),(2,'Biliran Chasing Waterfalls Eco-Trek','A full-day thrilling adventure traversing Tinago Falls, Ulan-Ulan Falls, and Reco Falls with habal-habal transport and native lunch.','PRE_DESIGNED',1250.00,1,2,8,'Habal-habal transport, safety helmets, entrance fees to 3 falls, local trek guide, native lunch','Tips and personal souvenirs','ACTIVE','package_waterfalls.jpg',1,'2026-11-11 11:43:26'),(3,'Higatangan Shifting Sandbar & Heritage Cruise','Sail to Higatangan Island, walk the legendary shifting sandbar, and discover local weaving communities.','PRE_DESIGNED',1400.00,1,4,8,'Roundtrip chartered boat, environmental fee, seafood picnic, community guide','Alcoholic beverages','ACTIVE','package_higatangan.jpg',1,'2026-11-11 11:43:26'),(4,'Maripipi Artisan Culture & Pottery Immersion','Discover ancient pottery making in Bato, visit the historic watchtower, and sample authentic Maripipi delicacies.','PRE_DESIGNED',950.00,1,2,6,'Pottery clay workshop materials, local guide, afternoon native snack, village tour','Personal transport to Maripipi','ACTIVE','package_artisan.jpg',1,'2026-11-11 11:43:26');
/*!40000 ALTER TABLE `tourism_packages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tourism_services`
--

DROP TABLE IF EXISTS `tourism_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tourism_services` (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `stakeholder_id` int NOT NULL,
  `service_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `service_category` enum('GUIDING','TRANSPORT','BOAT_CHARTER','FOOD_CATERING','HANDICRAFT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `base_price` decimal(10,2) NOT NULL,
  `pricing_unit` enum('PER_PERSON','PER_GROUP','PER_TRIP','PER_ITEM') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PER_PERSON',
  `max_capacity` int NOT NULL DEFAULT '1',
  `is_available` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`service_id`),
  KEY `stakeholder_id` (`stakeholder_id`),
  CONSTRAINT `tourism_services_ibfk_1` FOREIGN KEY (`stakeholder_id`) REFERENCES `stakeholder_profiles` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tourism_services`
--

LOCK TABLES `tourism_services` WRITE;
/*!40000 ALTER TABLE `tourism_services` DISABLE KEYS */;
INSERT INTO `tourism_services` VALUES (1,1,'Sambawan Marine Sanctuary Snorkel & Viewpoint Guide','GUIDING','Guided trek to the iconic Sambawan hill viewpoint and certified coral reef snorkeling orientation.',500.00,'PER_GROUP',6,1,'2026-11-11 11:43:26'),(2,1,'Ulan-Ulan & Reco Falls Canyon Trekking Guide','GUIDING','Expert guided jungle trek navigating the lush rainforest trails of Sampao waterfalls.',600.00,'PER_GROUP',5,1,'2026-11-11 11:43:26'),(3,2,'Biliran Waterfalls Circuit Habal-Habal Tour (Driver & Fuel)','TRANSPORT','Full-day motorcycle transport with helmet covering Tinago, Ulan-Ulan, and Iyusan terraces.',800.00,'PER_PERSON',1,1,'2026-11-11 11:43:26'),(4,2,'Naval to Kawayan / Caibiran Tourist Van Transfer','TRANSPORT','Air-conditioned van charter for group transfers across mainland Biliran destinations.',2500.00,'PER_TRIP',12,1,'2026-11-11 11:43:26'),(5,3,'Kawayan Port to Sambawan Island Roundtrip Boat Charter','BOAT_CHARTER','Coast Guard approved motorized pumpboat with life vests for up to 10 passengers.',3000.00,'PER_TRIP',10,1,'2026-11-11 11:43:26'),(6,3,'Naval Port to Higatangan Shifting Sandbar Island Boat Charter','BOAT_CHARTER','Roundtrip scenic boat crossing to Higatangan with crystal clear waters and sandbar.',2200.00,'PER_TRIP',8,1,'2026-11-11 11:43:26'),(7,4,'Biliran Fiesta Seafood & Native Chicken Boodle Fight','FOOD_CATERING','Freshly prepared ocean catch, native inasal, sinigang sa batwan, organic rice, and fresh coconut.',350.00,'PER_PERSON',20,1,'2026-11-11 11:43:26'),(8,4,'Traditional Biliran Suman & Native Hot Chocolate Breakfast','FOOD_CATERING','Authentic sticky rice roll with latik syrup paired with artisanal tablea hot cocoa.',150.00,'PER_PERSON',15,1,'2026-11-11 11:43:26'),(9,5,'Traditional Maripipi Terracotta Pottery Workshop','HANDICRAFT','Hands-on molding experience using local Maripipi clay led by master local potters.',400.00,'PER_PERSON',8,1,'2026-11-11 11:43:26');
/*!40000 ALTER TABLE `tourism_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tourist_profiles`
--

DROP TABLE IF EXISTS `tourist_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tourist_profiles` (
  `profile_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `origin_city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nationality` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT 'Filipino',
  `emergency_contact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `travel_preferences` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `tourist_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tourist_profiles`
--

LOCK TABLES `tourist_profiles` WRITE;
/*!40000 ALTER TABLE `tourist_profiles` DISABLE KEYS */;
INSERT INTO `tourist_profiles` VALUES (1,10,'Cebu City','Filipino','Marco Cortez (Father)','+63 915 888 4444','Eco-tourism, island hopping, waterfall trekking, cultural handicraft exploration'),(2,11,'Tacloban','Filipino',NULL,NULL,NULL);
/*!40000 ALTER TABLE `tourist_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `salt` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role_id` int NOT NULL,
  `barangay_id` int DEFAULT NULL,
  `municipality_id` int DEFAULT NULL,
  `status` enum('ACTIVE','PENDING_APPROVAL','DEACTIVATED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `barangay_id` (`barangay_id`),
  KEY `idx_users_role` (`role_id`),
  KEY `idx_users_status` (`status`),
  KEY `fk_user_municipality` (`municipality_id`),
  CONSTRAINT `fk_user_municipality` FOREIGN KEY (`municipality_id`) REFERENCES `municipalities` (`municipality_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `users_ibfk_2` FOREIGN KEY (`barangay_id`) REFERENCES `barangays` (`barangay_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','admin@lakbaylokal.gov.ph','Administrator Maria Santos','+63 917 000 0001',1,9,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(2,'tourismoffice','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','tourism@biliran.gov.ph','Director Eduardo Reyes','+63 917 000 0002',2,9,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(3,'lgu_naval','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','lgu.naval@biliran.gov.ph','Officer Katrina Gonzales','+63 917 000 0003',3,9,1,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:44:02'),(4,'brgy_olog','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','brgy.olog@maripipi.gov.ph','Captain Ramon Corpin','+63 917 111 2001',4,1,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(5,'guide_pedro','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','pedro.guide@gmail.com','Pedro Penduko','+63 928 111 8888',5,1,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(6,'driver_juan','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','juan.transpo@gmail.com','Juan Dela Cruz','+63 928 222 9999',5,6,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(7,'boat_capitan','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','maripipi.boats@gmail.com','Capitan Danilo Flores','+63 928 333 7777',5,1,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(8,'cook_nanay_luz','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','nanayluz.catering@gmail.com','Luzviminda Ocampo','+63 928 444 6666',5,4,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(9,'artisan_rosa','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','maripipi.pottery@gmail.com','Rosa Mangahas','+63 928 555 5555',5,2,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(10,'tourist_ana','8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6','lakbaysalt2026','ana.tourist@gmail.com','Ana Sophia Cortez','+63 915 777 3333',6,NULL,NULL,'ACTIVE','2026-11-11 11:43:25','2026-11-11 11:43:25'),(11,'lexey','6c70129b7fafad2e1e4e4780cf7576ae529f4b0c29356b6f372a0e4d5ec5f68f','762ec00e1ff775c6eaef1d3af02644a3','romanialexa@gmail.com','Alexa Romania','+639262333060',6,NULL,NULL,'ACTIVE','2026-11-11 12:38:25','2026-11-11 12:38:25');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_barangay_tourism_overview`
--

DROP TABLE IF EXISTS `v_barangay_tourism_overview`;
/*!50001 DROP VIEW IF EXISTS `v_barangay_tourism_overview`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_barangay_tourism_overview` AS SELECT 
 1 AS `barangay_id`,
 1 AS `barangay_name`,
 1 AS `municipality_name`,
 1 AS `contact_person`,
 1 AS `contact_number`,
 1 AS `total_destinations`,
 1 AS `total_stakeholders`,
 1 AS `verified_stakeholders`,
 1 AS `pending_stakeholders`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_destination_footfall_summary`
--

DROP TABLE IF EXISTS `v_destination_footfall_summary`;
/*!50001 DROP VIEW IF EXISTS `v_destination_footfall_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_destination_footfall_summary` AS SELECT 
 1 AS `destination_id`,
 1 AS `destination_name`,
 1 AS `category`,
 1 AS `barangay_name`,
 1 AS `municipality_name`,
 1 AS `entrance_fee`,
 1 AS `status`,
 1 AS `total_local_visitors`,
 1 AS `total_domestic_visitors`,
 1 AS `total_foreign_visitors`,
 1 AS `grand_total_visitors`,
 1 AS `total_revenue_generated`,
 1 AS `average_rating`,
 1 AS `total_reviews_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_stakeholder_performance_summary`
--

DROP TABLE IF EXISTS `v_stakeholder_performance_summary`;
/*!50001 DROP VIEW IF EXISTS `v_stakeholder_performance_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_stakeholder_performance_summary` AS SELECT 
 1 AS `profile_id`,
 1 AS `business_name`,
 1 AS `category`,
 1 AS `stakeholder_owner`,
 1 AS `barangay_name`,
 1 AS `municipality_name`,
 1 AS `verification_status`,
 1 AS `active_services_count`,
 1 AS `total_service_bookings`,
 1 AS `estimated_gross_income`,
 1 AS `average_rating`,
 1 AS `review_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `verification_records`
--

DROP TABLE IF EXISTS `verification_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verification_records` (
  `verification_id` int NOT NULL AUTO_INCREMENT,
  `stakeholder_id` int NOT NULL,
  `barangay_id` int NOT NULL,
  `reviewer_id` int NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `requirements_submitted` text COLLATE utf8mb4_unicode_ci,
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `decision_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`verification_id`),
  KEY `stakeholder_id` (`stakeholder_id`),
  KEY `barangay_id` (`barangay_id`),
  KEY `reviewer_id` (`reviewer_id`),
  CONSTRAINT `verification_records_ibfk_1` FOREIGN KEY (`stakeholder_id`) REFERENCES `stakeholder_profiles` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `verification_records_ibfk_2` FOREIGN KEY (`barangay_id`) REFERENCES `barangays` (`barangay_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `verification_records_ibfk_3` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verification_records`
--

LOCK TABLES `verification_records` WRITE;
/*!40000 ALTER TABLE `verification_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `verification_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'lakbay_lokal_db'
--

--
-- Final view structure for view `v_barangay_tourism_overview`
--

/*!50001 DROP VIEW IF EXISTS `v_barangay_tourism_overview`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_unicode_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_barangay_tourism_overview` AS select `b`.`barangay_id` AS `barangay_id`,`b`.`name` AS `barangay_name`,`m`.`name` AS `municipality_name`,`b`.`contact_person` AS `contact_person`,`b`.`contact_number` AS `contact_number`,count(distinct `d`.`destination_id`) AS `total_destinations`,count(distinct `sp`.`profile_id`) AS `total_stakeholders`,count(distinct (case when (`sp`.`verification_status` = 'VERIFIED') then `sp`.`profile_id` end)) AS `verified_stakeholders`,count(distinct (case when (`sp`.`verification_status` = 'PENDING') then `sp`.`profile_id` end)) AS `pending_stakeholders` from (((`barangays` `b` join `municipalities` `m` on((`b`.`municipality_id` = `m`.`municipality_id`))) left join `destinations` `d` on((`b`.`barangay_id` = `d`.`barangay_id`))) left join `stakeholder_profiles` `sp` on((`b`.`barangay_id` = `sp`.`barangay_id`))) group by `b`.`barangay_id`,`b`.`name`,`m`.`name`,`b`.`contact_person`,`b`.`contact_number` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_destination_footfall_summary`
--

/*!50001 DROP VIEW IF EXISTS `v_destination_footfall_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_unicode_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_destination_footfall_summary` AS select `d`.`destination_id` AS `destination_id`,`d`.`name` AS `destination_name`,`d`.`category` AS `category`,`b`.`name` AS `barangay_name`,`m`.`name` AS `municipality_name`,`d`.`entrance_fee` AS `entrance_fee`,`d`.`status` AS `status`,coalesce(sum(`al`.`local_tourists_count`),0) AS `total_local_visitors`,coalesce(sum(`al`.`domestic_tourists_count`),0) AS `total_domestic_visitors`,coalesce(sum(`al`.`foreign_tourists_count`),0) AS `total_foreign_visitors`,coalesce(sum(((`al`.`local_tourists_count` + `al`.`domestic_tourists_count`) + `al`.`foreign_tourists_count`)),0) AS `grand_total_visitors`,coalesce(sum(`al`.`total_revenue`),0) AS `total_revenue_generated`,coalesce(round(avg(`r`.`rating`),1),5.0) AS `average_rating`,count(distinct `r`.`review_id`) AS `total_reviews_count` from ((((`destinations` `d` join `barangays` `b` on((`d`.`barangay_id` = `b`.`barangay_id`))) join `municipalities` `m` on((`b`.`municipality_id` = `m`.`municipality_id`))) left join `tourism_arrival_logs` `al` on((`d`.`destination_id` = `al`.`destination_id`))) left join `reviews` `r` on(((`r`.`target_type` = 'DESTINATION') and (`r`.`target_id` = `d`.`destination_id`)))) group by `d`.`destination_id`,`d`.`name`,`d`.`category`,`b`.`name`,`m`.`name`,`d`.`entrance_fee`,`d`.`status` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_stakeholder_performance_summary`
--

/*!50001 DROP VIEW IF EXISTS `v_stakeholder_performance_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_unicode_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_stakeholder_performance_summary` AS select `sp`.`profile_id` AS `profile_id`,`sp`.`business_name` AS `business_name`,`sp`.`category` AS `category`,`u`.`full_name` AS `stakeholder_owner`,`b`.`name` AS `barangay_name`,`m`.`name` AS `municipality_name`,`sp`.`verification_status` AS `verification_status`,count(distinct `ts`.`service_id`) AS `active_services_count`,count(distinct `bsi`.`item_id`) AS `total_service_bookings`,coalesce(sum(`bsi`.`subtotal`),0.00) AS `estimated_gross_income`,coalesce(round(avg(`r`.`rating`),1),5.0) AS `average_rating`,count(distinct `r`.`review_id`) AS `review_count` from ((((((`stakeholder_profiles` `sp` join `users` `u` on((`sp`.`user_id` = `u`.`user_id`))) join `barangays` `b` on((`sp`.`barangay_id` = `b`.`barangay_id`))) join `municipalities` `m` on((`b`.`municipality_id` = `m`.`municipality_id`))) left join `tourism_services` `ts` on((`sp`.`profile_id` = `ts`.`stakeholder_id`))) left join `booking_service_items` `bsi` on(((`ts`.`service_id` = `bsi`.`service_id`) and (`bsi`.`status` in ('ACCEPTED','COMPLETED'))))) left join `reviews` `r` on(((`r`.`target_type` = 'STAKEHOLDER') and (`r`.`target_id` = `sp`.`profile_id`)))) group by `sp`.`profile_id`,`sp`.`business_name`,`sp`.`category`,`u`.`full_name`,`b`.`name`,`m`.`name`,`sp`.`verification_status` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-11 20:58:56
