-- ========================================================
-- LAKBAY LOKAL DATABASE BACKUP DUMP
-- Generated At: Sun Nov 08 08:51:14 PST 2026
-- ========================================================
SET FOREIGN_KEY_CHECKS = 0;

-- Table: announcements
INSERT INTO `announcements` VALUES (1, 'Biliran Province 34th Foundation Anniversary & Tourism Expo', 'Join us from May 7 to 11 in Naval for the annual Province-Wide Tourism Expo, featuring local culinary showcases, artisanal handicrafts, and eco-tour discounts.', 'FESTIVAL', NULL, 2, '2026-04-01', '2026-05-15', 'PUBLISHED', '2026-10-13 20:58:02.0');
INSERT INTO `announcements` VALUES (2, 'Sustainable Eco-Trekking Guidelines for Ulan-Ulan and Tinago Falls', 'All visitors are reminded to observe Leave No Trace principles: single-use plastics are prohibited within 50 meters of water basins. Certified community guides are mandatory.', 'TRAVEL_GUIDELINE', 6, 2, '2026-02-01', '2026-12-31', 'PUBLISHED', '2026-10-13 20:58:02.0');
INSERT INTO `announcements` VALUES (3, 'Coast Guard Sea Travel Advisory: Calm Seas at Maripipi & Sambawan', 'Philippine Coast Guard reports favorable sea conditions across the Biliran Strait. Motorized tourist vessels from Kawayan and Naval are cleared for regular island crossings.', 'ANNOUNCEMENT', 1, 2, '2026-03-01', '2026-04-30', 'PUBLISHED', '2026-10-13 20:58:02.0');

-- Table: audit_logs
INSERT INTO `audit_logs` VALUES (1, 1, 'admin', 'SYSTEM_INITIALIZATION', 'DATABASE', 1, '127.0.0.1', 'Lakbay Lokal database initialized with seed data for Biliran Province.', '2026-01-01 00:00:00.0');
INSERT INTO `audit_logs` VALUES (2, 4, 'brgy_olog', 'STAKEHOLDER_VERIFIED', 'STAKEHOLDER_PROFILE', 1, '127.0.0.1', 'Barangay Captain Ramon Corpin verified Sambawan Island Eco-Trek Guides.', '2026-01-15 09:30:00.0');
INSERT INTO `audit_logs` VALUES (3, 10, 'tourist_ana', 'CREATE_BOOKING', 'BOOKINGS', 1, '127.0.0.1', 'Created booking reference LL-2026-0001 for Sambawan Island Paradise Escape.', '2026-02-15 10:00:00.0');
INSERT INTO `audit_logs` VALUES (4, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:44:01.0');
INSERT INTO `audit_logs` VALUES (5, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:19.0');
INSERT INTO `audit_logs` VALUES (6, 1, 'admin', 'LOGIN_SUCCESS', 'AUTH', 1, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:20.0');
INSERT INTO `audit_logs` VALUES (7, 2, 'tourismoffice', 'LOGIN_SUCCESS', 'AUTH', 2, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:20.0');
INSERT INTO `audit_logs` VALUES (8, 3, 'lgu_naval', 'LOGIN_SUCCESS', 'AUTH', 3, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:20.0');
INSERT INTO `audit_logs` VALUES (9, 4, 'brgy_olog', 'LOGIN_SUCCESS', 'AUTH', 4, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:20.0');
INSERT INTO `audit_logs` VALUES (10, 5, 'guide_pedro', 'LOGIN_SUCCESS', 'AUTH', 5, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:20.0');
INSERT INTO `audit_logs` VALUES (11, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 18:47:20.0');
INSERT INTO `audit_logs` VALUES (12, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (13, 1, 'admin', 'LOGIN_SUCCESS', 'AUTH', 1, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (14, 2, 'tourismoffice', 'LOGIN_SUCCESS', 'AUTH', 2, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (15, 3, 'lgu_naval', 'LOGIN_SUCCESS', 'AUTH', 3, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (16, 4, 'brgy_olog', 'LOGIN_SUCCESS', 'AUTH', 4, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (17, 5, 'guide_pedro', 'LOGIN_SUCCESS', 'AUTH', 5, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (18, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '127.0.0.1', 'User logged in from 127.0.0.1', '2026-10-27 19:06:19.0');
INSERT INTO `audit_logs` VALUES (19, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '0:0:0:0:0:0:0:1', 'User logged in', '2026-10-27 19:09:35.0');
INSERT INTO `audit_logs` VALUES (20, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '0:0:0:0:0:0:0:1', 'User logged in', '2026-11-08 08:40:56.0');
INSERT INTO `audit_logs` VALUES (21, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '0:0:0:0:0:0:0:1', 'User logged in', '2026-11-08 08:41:07.0');
INSERT INTO `audit_logs` VALUES (22, 10, 'tourist_ana', 'LOGIN_SUCCESS', 'AUTH', 10, '0:0:0:0:0:0:0:1', 'User logged in', '2026-11-08 08:41:09.0');
INSERT INTO `audit_logs` VALUES (23, 2, 'tourismoffice', 'LOGIN_SUCCESS', 'AUTH', 2, '0:0:0:0:0:0:0:1', 'User logged in', '2026-11-08 08:41:13.0');
INSERT INTO `audit_logs` VALUES (24, 2, 'tourismoffice', 'LOGIN_SUCCESS', 'AUTH', 2, '0:0:0:0:0:0:0:1', 'User logged in', '2026-11-08 08:41:15.0');
INSERT INTO `audit_logs` VALUES (25, 2, 'tourismoffice', 'LOGIN_SUCCESS', 'AUTH', 2, '0:0:0:0:0:0:0:1', 'User logged in', '2026-11-08 08:41:16.0');

-- Table: barangays
INSERT INTO `barangays` VALUES (1, 8, 'Ol-og (Sambawan)', 'Hon. Ramon Corpin', '+63 917 111 2001');
INSERT INTO `barangays` VALUES (2, 8, 'Bato', 'Hon. Elena Tan', '+63 917 111 2002');
INSERT INTO `barangays` VALUES (3, 8, 'Ermita', 'Hon. Lucas Del Rosario', '+63 917 111 2003');
INSERT INTO `barangays` VALUES (4, 5, 'Cabibihan (Tinago)', 'Hon. Roberto Gomez', '+63 918 222 3001');
INSERT INTO `barangays` VALUES (5, 5, 'Manlabang', 'Hon. Corazon Santos', '+63 918 222 3002');
INSERT INTO `barangays` VALUES (6, 2, 'Sampao (Ulan-Ulan)', 'Hon. Mario Morales', '+63 919 333 4001');
INSERT INTO `barangays` VALUES (7, 2, 'Iyusan', 'Hon. Teresa Cruz', '+63 919 333 4002');
INSERT INTO `barangays` VALUES (8, 2, 'Kawayanon', 'Hon. Daniel Navarro', '+63 919 333 4003');
INSERT INTO `barangays` VALUES (9, 1, 'P.I. Garcia', 'Hon. Fernando Perez', '+63 920 444 5001');
INSERT INTO `barangays` VALUES (10, 1, 'Smo. Rosario (Higatangan)', 'Hon. Gabriel Lim', '+63 920 444 5002');
INSERT INTO `barangays` VALUES (11, 7, 'Busali', 'Hon. Gloria Bautista', '+63 921 555 6001');
INSERT INTO `barangays` VALUES (12, 3, 'Baganito', 'Hon. Angela Rivera', '+63 922 666 7001');

-- Table: booking_service_items

-- Table: bookings
INSERT INTO `bookings` VALUES (1, 'LL-2026-0001', 1, 1, '2026-02-15 10:00:00.0', '2026-03-20', '2026-03-21', 4, 7400.00, 'COMPLETED', 'Group of 4 photography enthusiasts, requesting sunset trek at Sambawan hill.', '2026-10-13 20:58:02.0', '2026-10-13 20:58:02.0');
INSERT INTO `bookings` VALUES (2, 'LL-2026-0002', 1, 2, '2026-03-01 14:30:00.0', '2026-04-10', '2026-04-10', 2, 2500.00, 'CONFIRMED', 'Vegetarian lunch request for one guest.', '2026-10-13 20:58:02.0', '2026-10-13 20:58:02.0');
INSERT INTO `bookings` VALUES (3, 'LL-2026-0003', 1, NULL, '2026-03-05 09:15:00.0', '2026-04-18', '2026-04-18', 3, 2100.00, 'PENDING', 'Custom booking: Habal-habal tour plus Pottery workshop.', '2026-10-13 20:58:02.0', '2026-10-13 20:58:02.0');

-- Table: destinations
INSERT INTO `destinations` VALUES (1, 1, 'Sambawan Island Marine Sanctuary', 'ISLAND_BEACH', 'A world-class semi-curved island paradise featuring rocky volcanic peaks, pristine white sand beaches, rich coral marine sanctuary, and a scenic observation tower.', 120.00, 'Open 24 Hours (Overnight Camping Available)', '11.6025, 124.2628', 'maripipi.tourism@biliran.gov.ph', 'sambawan_island.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (2, 4, 'Tinago Falls Eco-Park', 'WATERFALL', 'A majestic waterfall gracefully cascading into a deep, turquoise blue natural pool framed by lush jungle foliage and wooden picnic cottages.', 50.00, '7:00 AM - 5:00 PM', '11.5367, 124.5244', 'tinago.caibiran@biliran.gov.ph', 'tinago_falls.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (3, 6, 'Ulan-Ulan Falls', 'WATERFALL', 'A 25-meter roaring waterfall whose spray resembles a gentle rain shower (ulan), nestled in a tropical ravine perfect for nature enthusiasts.', 40.00, '6:30 AM - 4:30 PM', '11.5839, 124.4172', 'almeria.tourism@biliran.gov.ph', 'ulan_ulan_falls.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (4, 10, 'Higatangan Island & Shifting Sandbar', 'ISLAND_BEACH', 'An island marvel featuring a 200-meter white sandbar that magically shifts direction with ocean tides, clear snorkeling waters, and volcanic rock formations.', 80.00, '6:00 AM - 6:00 PM', '11.5647, 124.2811', 'naval.tourism@biliran.gov.ph', 'higatangan_sandbar.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (5, 5, 'Mainit Hot Spring', 'HOT_SPRING', 'Natural therapeutic mineral hot springs flowing down a geothermal river bed with temperature pools designed for relaxation and healing.', 40.00, '7:00 AM - 7:00 PM', '11.5511, 124.5422', 'caibiran.hotspring@biliran.gov.ph', 'mainit_spring.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (6, 7, 'Iyusan Rice Terraces', 'CULTURAL_HERITAGE', 'Spectacular centuries-old stone-walled rice terraces carved onto verdant mountain slopes reflecting community agricultural heritage.', 25.00, '6:00 AM - 5:30 PM', '11.5714, 124.4022', 'iyusan.brgy@almeria.gov.ph', 'iyusan_terraces.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (7, 2, 'Maripipi Pottery & Cultural Village', 'CULTURAL_HERITAGE', 'Traditional community hub where generational pottery artisans mold natural red clay pots (daba) and clay ovens using indigenous techniques.', 30.00, '8:00 AM - 4:00 PM', '11.7708, 124.3411', 'maripipi.artisan@biliran.gov.ph', 'maripipi_pottery.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');
INSERT INTO `destinations` VALUES (8, 6, 'Reco Falls', 'WATERFALL', 'A serene and tranquil swimming hole located upstream from Ulan-Ulan falls, offering calm emerald waters and natural diving ledges.', 30.00, '7:00 AM - 4:30 PM', '11.5894, 124.4219', 'almeria.tourism@biliran.gov.ph', 'reco_falls.jpg', 'ACTIVE', 1, '2026-01-10T10:00', '2026-10-13 20:58:02.0');

-- Table: municipalities
INSERT INTO `municipalities` VALUES (1, 'Naval', 'Biliran', '6560');
INSERT INTO `municipalities` VALUES (2, 'Almeria', 'Biliran', '6561');
INSERT INTO `municipalities` VALUES (3, 'Kawayan', 'Biliran', '6562');
INSERT INTO `municipalities` VALUES (4, 'Culaba', 'Biliran', '6563');
INSERT INTO `municipalities` VALUES (5, 'Caibiran', 'Biliran', '6564');
INSERT INTO `municipalities` VALUES (6, 'Cabucgayan', 'Biliran', '6565');
INSERT INTO `municipalities` VALUES (7, 'Biliran', 'Biliran', '6566');
INSERT INTO `municipalities` VALUES (8, 'Maripipi', 'Biliran', '6567');

-- Table: package_destinations
INSERT INTO `package_destinations` VALUES (1, 1, 1);
INSERT INTO `package_destinations` VALUES (2, 2, 1);
INSERT INTO `package_destinations` VALUES (2, 3, 2);
INSERT INTO `package_destinations` VALUES (2, 8, 3);
INSERT INTO `package_destinations` VALUES (3, 4, 1);
INSERT INTO `package_destinations` VALUES (4, 7, 1);

-- Table: package_services
INSERT INTO `package_services` VALUES (1, 1);
INSERT INTO `package_services` VALUES (2, 2);
INSERT INTO `package_services` VALUES (2, 3);
INSERT INTO `package_services` VALUES (1, 5);
INSERT INTO `package_services` VALUES (3, 6);
INSERT INTO `package_services` VALUES (1, 7);
INSERT INTO `package_services` VALUES (4, 9);

-- Table: reviews
INSERT INTO `reviews` VALUES (1, 1, 1, 'PACKAGE', 1, 5, 'Sambawan Island is sheer paradise! The boat ride was smooth, our guide Kuya Pedro took magnificent photos, and the seafood boodle fight was delicious!', '2026-03-22 08:30:00.0');
INSERT INTO `reviews` VALUES (2, 1, 1, 'STAKEHOLDER', 1, 5, 'Kuya Pedro is very knowledgeable about the coral reefs and ensured all of us wore life jackets while snorkeling. Truly community-based excellence!', '2026-03-22 08:35:00.0');
INSERT INTO `reviews` VALUES (3, 1, 1, 'DESTINATION', 1, 5, 'The view from the Sambawan hill at sunrise took our breath away. Clean facilities and well-maintained trail.', '2026-03-22 08:40:00.0');

-- Table: roles
INSERT INTO `roles` VALUES (1, 'SYSTEM_ADMIN', 'System Administrator', 'Full administrative authority across all modules, roles, security, and databases.');
INSERT INTO `roles` VALUES (2, 'TOURISM_OFFICE', 'Tourism Office Officer', 'Province-wide promotion, announcements, arrival trend analytics, and project supervision.');
INSERT INTO `roles` VALUES (3, 'LGU_OFFICER', 'Local Government Unit Officer', 'Municipal-level tourism monitoring, inter-barangay coordination, and consolidated reporting.');
INSERT INTO `roles` VALUES (4, 'BARANGAY_OFFICIAL', 'Barangay Official', 'Local stakeholder verification, attraction validation, footfall logging, and barangay records.');
INSERT INTO `roles` VALUES (5, 'STAKEHOLDER', 'Local Tourism Stakeholder', 'Community guides, drivers, boat operators, cooks, and artisans managing services and bookings.');
INSERT INTO `roles` VALUES (6, 'TOURIST', 'Tourist', 'Visitors exploring destinations, customizing packages, booking services, and leaving reviews.');

-- Table: stakeholder_profiles
INSERT INTO `stakeholder_profiles` VALUES (1, 5, 1, 'Sambawan Island Eco-Trek & Marine Guides', 'TOUR_GUIDE', 'DOT-R8-TG-2024-001', '+63 928 111 8888', 6, 'VERIFIED', 4, '2026-01-15T09:30');
INSERT INTO `stakeholder_profiles` VALUES (2, 6, 6, 'Almeria Habal-Habal & Van Adventure Transport', 'TRANSPORT_DRIVER', 'LGU-ALM-TR-2025-014', '+63 928 222 9999', 4, 'VERIFIED', 4, '2026-02-10T14:00');
INSERT INTO `stakeholder_profiles` VALUES (3, 7, 1, 'Maripipi Seafarers Motorized Pumpboat Cooperative', 'BOAT_OPERATOR', 'MARINA-EV-2024-099', '+63 928 333 7777', 10, 'VERIFIED', 4, '2026-01-20T11:15');
INSERT INTO `stakeholder_profiles` VALUES (4, 8, 4, 'Nanay Luz Traditional Biliran Kakanin & Beach Feasts', 'LOCAL_COOK', 'BRGY-CAB-LK-2025-003', '+63 928 444 6666', 8, 'VERIFIED', 4, '2026-02-05T10:00');
INSERT INTO `stakeholder_profiles` VALUES (5, 9, 2, 'Maripipi Heritage Clay Pottery & Woven Handicrafts', 'ARTISAN', 'DTI-BIL-ART-2024-042', '+63 928 555 5555', 12, 'VERIFIED', 4, '2026-01-18T16:45');

-- Table: tourism_arrival_logs
INSERT INTO `tourism_arrival_logs` VALUES (1, 1, '2026-03-01', 45, 120, 18, 21960.00, 4, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_arrival_logs` VALUES (2, 1, '2026-03-02', 30, 85, 12, 15240.00, 4, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_arrival_logs` VALUES (3, 2, '2026-03-01', 60, 140, 5, 10250.00, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_arrival_logs` VALUES (4, 3, '2026-03-01', 25, 90, 8, 4920.00, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_arrival_logs` VALUES (5, 4, '2026-03-01', 20, 65, 4, 7120.00, 1, '2026-10-13 20:58:02.0');

-- Table: tourism_packages
INSERT INTO `tourism_packages` VALUES (1, 'Sambawan Island Marine Paradise Escape', 'Experience BiliranÔÇÖs premier marine sanctuary with roundtrip boat charter from Kawayan, hilltop viewpoint trek, snorkeling guide, and beach boodle feast.', 'PRE_DESIGNED', 1850.00, 2, 4, 10, 'Boat charter, entrance fees, certified guide, camping tent/cottage, seafood lunch feast', 'Personal snacks, snorkeling gear rental', 'ACTIVE', 'package_sambawan.jpg', 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_packages` VALUES (2, 'Biliran Chasing Waterfalls Eco-Trek', 'A full-day thrilling adventure traversing Tinago Falls, Ulan-Ulan Falls, and Reco Falls with habal-habal transport and native lunch.', 'PRE_DESIGNED', 1250.00, 1, 2, 8, 'Habal-habal transport, safety helmets, entrance fees to 3 falls, local trek guide, native lunch', 'Tips and personal souvenirs', 'ACTIVE', 'package_waterfalls.jpg', 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_packages` VALUES (3, 'Higatangan Shifting Sandbar & Heritage Cruise', 'Sail to Higatangan Island, walk the legendary shifting sandbar, and discover local weaving communities.', 'PRE_DESIGNED', 1400.00, 1, 4, 8, 'Roundtrip chartered boat, environmental fee, seafood picnic, community guide', 'Alcoholic beverages', 'ACTIVE', 'package_higatangan.jpg', 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_packages` VALUES (4, 'Maripipi Artisan Culture & Pottery Immersion', 'Discover ancient pottery making in Bato, visit the historic watchtower, and sample authentic Maripipi delicacies.', 'PRE_DESIGNED', 950.00, 1, 2, 6, 'Pottery clay workshop materials, local guide, afternoon native snack, village tour', 'Personal transport to Maripipi', 'ACTIVE', 'package_artisan.jpg', 1, '2026-10-13 20:58:02.0');

-- Table: tourism_services
INSERT INTO `tourism_services` VALUES (1, 1, 'Sambawan Marine Sanctuary Snorkel & Viewpoint Guide', 'GUIDING', 'Guided trek to the iconic Sambawan hill viewpoint and certified coral reef snorkeling orientation.', 500.00, 'PER_GROUP', 6, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (2, 1, 'Ulan-Ulan & Reco Falls Canyon Trekking Guide', 'GUIDING', 'Expert guided jungle trek navigating the lush rainforest trails of Sampao waterfalls.', 600.00, 'PER_GROUP', 5, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (3, 2, 'Biliran Waterfalls Circuit Habal-Habal Tour (Driver & Fuel)', 'TRANSPORT', 'Full-day motorcycle transport with helmet covering Tinago, Ulan-Ulan, and Iyusan terraces.', 800.00, 'PER_PERSON', 1, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (4, 2, 'Naval to Kawayan / Caibiran Tourist Van Transfer', 'TRANSPORT', 'Air-conditioned van charter for group transfers across mainland Biliran destinations.', 2500.00, 'PER_TRIP', 12, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (5, 3, 'Kawayan Port to Sambawan Island Roundtrip Boat Charter', 'BOAT_CHARTER', 'Coast Guard approved motorized pumpboat with life vests for up to 10 passengers.', 3000.00, 'PER_TRIP', 10, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (6, 3, 'Naval Port to Higatangan Shifting Sandbar Island Boat Charter', 'BOAT_CHARTER', 'Roundtrip scenic boat crossing to Higatangan with crystal clear waters and sandbar.', 2200.00, 'PER_TRIP', 8, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (7, 4, 'Biliran Fiesta Seafood & Native Chicken Boodle Fight', 'FOOD_CATERING', 'Freshly prepared ocean catch, native inasal, sinigang sa batwan, organic rice, and fresh coconut.', 350.00, 'PER_PERSON', 20, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (8, 4, 'Traditional Biliran Suman & Native Hot Chocolate Breakfast', 'FOOD_CATERING', 'Authentic sticky rice roll with latik syrup paired with artisanal tablea hot cocoa.', 150.00, 'PER_PERSON', 15, 1, '2026-10-13 20:58:02.0');
INSERT INTO `tourism_services` VALUES (9, 5, 'Traditional Maripipi Terracotta Pottery Workshop', 'HANDICRAFT', 'Hands-on molding experience using local Maripipi clay led by master local potters.', 400.00, 'PER_PERSON', 8, 1, '2026-10-13 20:58:02.0');

-- Table: tourist_profiles
INSERT INTO `tourist_profiles` VALUES (1, 10, 'Cebu City', 'Filipino', 'Marco Cortez (Father)', '+63 915 888 4444', 'Eco-tourism, island hopping, waterfall trekking, cultural handicraft exploration');

-- Table: users
INSERT INTO `users` VALUES (1, 'admin', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'admin@lakbaylokal.gov.ph', 'Administrator Maria Santos', '+63 917 000 0001', 1, 9, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (2, 'tourismoffice', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'tourism@biliran.gov.ph', 'Director Eduardo Reyes', '+63 917 000 0002', 2, 9, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (3, 'lgu_naval', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'lgu.naval@biliran.gov.ph', 'Officer Katrina Gonzales', '+63 917 000 0003', 3, 9, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (4, 'brgy_olog', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'brgy.olog@maripipi.gov.ph', 'Captain Ramon Corpin', '+63 917 111 2001', 4, 1, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (5, 'guide_pedro', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'pedro.guide@gmail.com', 'Pedro Penduko', '+63 928 111 8888', 5, 1, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (6, 'driver_juan', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'juan.transpo@gmail.com', 'Juan Dela Cruz', '+63 928 222 9999', 5, 6, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (7, 'boat_capitan', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'maripipi.boats@gmail.com', 'Capitan Danilo Flores', '+63 928 333 7777', 5, 1, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (8, 'cook_nanay_luz', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'nanayluz.catering@gmail.com', 'Luzviminda Ocampo', '+63 928 444 6666', 5, 4, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (9, 'artisan_rosa', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'maripipi.pottery@gmail.com', 'Rosa Mangahas', '+63 928 555 5555', 5, 2, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');
INSERT INTO `users` VALUES (10, 'tourist_ana', '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6', 'lakbaysalt2026', 'ana.tourist@gmail.com', 'Ana Sophia Cortez', '+63 915 777 3333', 6, NULL, 'ACTIVE', '2026-10-13 20:58:02.0', '2026-10-27 18:38:35.0');

-- Table: verification_records

SET FOREIGN_KEY_CHECKS = 1;
-- Backup completed successfully.
