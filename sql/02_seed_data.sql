-- ============================================================================
-- LAKBAY LOKAL: SEED DATA SCRIPT
-- Target Location: Biliran Province, Philippines
-- Default testing password for all pre-seeded accounts: "password123"
-- Salt: "lakbaysalt2026"
-- Hash: SHA-256 of "password123lakbaysalt2026" =
-- 4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853
-- ============================================================================

USE lakbay_lokal_db;

-- ----------------------------------------------------------------------------
-- 1. SEED ROLES
-- ----------------------------------------------------------------------------
INSERT INTO roles (role_id, role_code, role_name, description) VALUES
(1, 'SYSTEM_ADMIN', 'System Administrator', 'Full administrative authority across all modules, roles, security, and databases.'),
(2, 'TOURISM_OFFICE', 'Tourism Office Officer', 'Province-wide promotion, announcements, arrival trend analytics, and project supervision.'),
(3, 'LGU_OFFICER', 'Local Government Unit Officer', 'Municipal-level tourism monitoring, inter-barangay coordination, and consolidated reporting.'),
(4, 'BARANGAY_OFFICIAL', 'Barangay Official', 'Local stakeholder verification, attraction validation, footfall logging, and barangay records.'),
(5, 'STAKEHOLDER', 'Local Tourism Stakeholder', 'Community guides, drivers, boat operators, cooks, and artisans managing services and bookings.'),
(6, 'TOURIST', 'Tourist', 'Visitors exploring destinations, customizing packages, booking services, and leaving reviews.')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- ----------------------------------------------------------------------------
-- 2. SEED MUNICIPALITIES (8 Municipalities of Biliran)
-- ----------------------------------------------------------------------------
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
-- 3. SEED BARANGAYS
-- ----------------------------------------------------------------------------
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
-- 4. SEED USERS (Password for all: password123)
-- ----------------------------------------------------------------------------
INSERT INTO users (user_id, username, password_hash, salt, email, full_name, phone, role_id, barangay_id, status) VALUES
(1, 'admin', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'admin@lakbaylokal.gov.ph', 'Administrator Maria Santos', '+63 917 000 0001', 1, 9, 'ACTIVE'),
(2, 'tourismoffice', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'tourism@biliran.gov.ph', 'Director Eduardo Reyes', '+63 917 000 0002', 2, 9, 'ACTIVE'),
(3, 'lgu_naval', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'lgu.naval@biliran.gov.ph', 'Officer Katrina Gonzales', '+63 917 000 0003', 3, 9, 'ACTIVE'),
(4, 'brgy_olog', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'brgy.olog@maripipi.gov.ph', 'Captain Ramon Corpin', '+63 917 111 2001', 4, 1, 'ACTIVE'),
(5, 'guide_pedro', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'pedro.guide@gmail.com', 'Pedro Penduko', '+63 928 111 8888', 5, 1, 'ACTIVE'),
(6, 'driver_juan', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'juan.transpo@gmail.com', 'Juan Dela Cruz', '+63 928 222 9999', 5, 6, 'ACTIVE'),
(7, 'boat_capitan', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'maripipi.boats@gmail.com', 'Capitan Danilo Flores', '+63 928 333 7777', 5, 1, 'ACTIVE'),
(8, 'cook_nanay_luz', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'nanayluz.catering@gmail.com', 'Luzviminda Ocampo', '+63 928 444 6666', 5, 4, 'ACTIVE'),
(9, 'artisan_rosa', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'maripipi.pottery@gmail.com', 'Rosa Mangahas', '+63 928 555 5555', 5, 2, 'ACTIVE'),
(10, 'tourist_ana', '4f686c125d0c7f21508933b91ef5220c3886be737190db72429dc1eb2eb1c853', 'lakbaysalt2026', 'ana.tourist@gmail.com', 'Ana Sophia Cortez', '+63 915 777 3333', 6, NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- ----------------------------------------------------------------------------
-- 5. SEED TOURIST PROFILES
-- ----------------------------------------------------------------------------
INSERT INTO tourist_profiles (profile_id, user_id, origin_city, nationality, emergency_contact, emergency_phone, travel_preferences) VALUES
(1, 10, 'Cebu City', 'Filipino', 'Marco Cortez (Father)', '+63 915 888 4444', 'Eco-tourism, island hopping, waterfall trekking, cultural handicraft exploration')
ON DUPLICATE KEY UPDATE origin_city = VALUES(origin_city);

-- ----------------------------------------------------------------------------
-- 6. SEED STAKEHOLDER PROFILES
-- ----------------------------------------------------------------------------
INSERT INTO stakeholder_profiles (profile_id, user_id, barangay_id, business_name, category, accreditation_no, contact_number, years_experience, verification_status, verified_by, verified_at) VALUES
(1, 5, 1, 'Sambawan Island Eco-Trek & Marine Guides', 'TOUR_GUIDE', 'DOT-R8-TG-2024-001', '+63 928 111 8888', 6, 'VERIFIED', 4, '2026-01-15 09:30:00'),
(2, 6, 6, 'Almeria Habal-Habal & Van Adventure Transport', 'TRANSPORT_DRIVER', 'LGU-ALM-TR-2025-014', '+63 928 222 9999', 4, 'VERIFIED', 4, '2026-02-10 14:00:00'),
(3, 7, 1, 'Maripipi Seafarers Motorized Pumpboat Cooperative', 'BOAT_OPERATOR', 'MARINA-EV-2024-099', '+63 928 333 7777', 10, 'VERIFIED', 4, '2026-01-20 11:15:00'),
(4, 8, 4, 'Nanay Luz Traditional Biliran Kakanin & Beach Feasts', 'LOCAL_COOK', 'BRGY-CAB-LK-2025-003', '+63 928 444 6666', 8, 'VERIFIED', 4, '2026-02-05 10:00:00'),
(5, 9, 2, 'Maripipi Heritage Clay Pottery & Woven Handicrafts', 'ARTISAN', 'DTI-BIL-ART-2024-042', '+63 928 555 5555', 12, 'VERIFIED', 4, '2026-01-18 16:45:00')
ON DUPLICATE KEY UPDATE business_name = VALUES(business_name);

-- ----------------------------------------------------------------------------
-- 7. SEED TOURISM SERVICES
-- ----------------------------------------------------------------------------
INSERT INTO tourism_services (service_id, stakeholder_id, service_name, service_category, description, base_price, pricing_unit, max_capacity, is_available) VALUES
(1, 1, 'Sambawan Marine Sanctuary Snorkel & Viewpoint Guide', 'GUIDING', 'Guided trek to the iconic Sambawan hill viewpoint and certified coral reef snorkeling orientation.', 500.00, 'PER_GROUP', 6, TRUE),
(2, 1, 'Ulan-Ulan & Reco Falls Canyon Trekking Guide', 'GUIDING', 'Expert guided jungle trek navigating the lush rainforest trails of Sampao waterfalls.', 600.00, 'PER_GROUP', 5, TRUE),
(3, 2, 'Biliran Waterfalls Circuit Habal-Habal Tour (Driver & Fuel)', 'TRANSPORT', 'Full-day motorcycle transport with helmet covering Tinago, Ulan-Ulan, and Iyusan terraces.', 800.00, 'PER_PERSON', 1, TRUE),
(4, 2, 'Naval to Kawayan / Caibiran Tourist Van Transfer', 'TRANSPORT', 'Air-conditioned van charter for group transfers across mainland Biliran destinations.', 2500.00, 'PER_TRIP', 12, TRUE),
(5, 3, 'Kawayan Port to Sambawan Island Roundtrip Boat Charter', 'BOAT_CHARTER', 'Coast Guard approved motorized pumpboat with life vests for up to 10 passengers.', 3000.00, 'PER_TRIP', 10, TRUE),
(6, 3, 'Naval Port to Higatangan Shifting Sandbar Island Boat Charter', 'BOAT_CHARTER', 'Roundtrip scenic boat crossing to Higatangan with crystal clear waters and sandbar.', 2200.00, 'PER_TRIP', 8, TRUE),
(7, 4, 'Biliran Fiesta Seafood & Native Chicken Boodle Fight', 'FOOD_CATERING', 'Freshly prepared ocean catch, native inasal, sinigang sa batwan, organic rice, and fresh coconut.', 350.00, 'PER_PERSON', 20, TRUE),
(8, 4, 'Traditional Biliran Suman & Native Hot Chocolate Breakfast', 'FOOD_CATERING', 'Authentic sticky rice roll with latik syrup paired with artisanal tablea hot cocoa.', 150.00, 'PER_PERSON', 15, TRUE),
(9, 5, 'Traditional Maripipi Terracotta Pottery Workshop', 'HANDICRAFT', 'Hands-on molding experience using local Maripipi clay led by master local potters.', 400.00, 'PER_PERSON', 8, TRUE)
ON DUPLICATE KEY UPDATE service_name = VALUES(service_name);

-- ----------------------------------------------------------------------------
-- 8. SEED DESTINATIONS (Famous Biliran Spots)
-- ----------------------------------------------------------------------------
INSERT INTO destinations (destination_id, barangay_id, name, category, description, entrance_fee, operating_hours, location_coordinates, contact_info, image_url, status, validated_by, validated_at) VALUES
(1, 1, 'Sambawan Island Marine Sanctuary', 'ISLAND_BEACH', 'A world-class semi-curved island paradise featuring rocky volcanic peaks, pristine white sand beaches, rich coral marine sanctuary, and a scenic observation tower.', 120.00, 'Open 24 Hours (Overnight Camping Available)', '11.6025, 124.2628', 'maripipi.tourism@biliran.gov.ph', 'sambawan_island.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(2, 4, 'Tinago Falls Eco-Park', 'WATERFALL', 'A majestic waterfall gracefully cascading into a deep, turquoise blue natural pool framed by lush jungle foliage and wooden picnic cottages.', 50.00, '7:00 AM - 5:00 PM', '11.5367, 124.5244', 'tinago.caibiran@biliran.gov.ph', 'tinago_falls.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(3, 6, 'Ulan-Ulan Falls', 'WATERFALL', 'A 25-meter roaring waterfall whose spray resembles a gentle rain shower (ulan), nestled in a tropical ravine perfect for nature enthusiasts.', 40.00, '6:30 AM - 4:30 PM', '11.5839, 124.4172', 'almeria.tourism@biliran.gov.ph', 'ulan_ulan_falls.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(4, 10, 'Higatangan Island & Shifting Sandbar', 'ISLAND_BEACH', 'An island marvel featuring a 200-meter white sandbar that magically shifts direction with ocean tides, clear snorkeling waters, and volcanic rock formations.', 80.00, '6:00 AM - 6:00 PM', '11.5647, 124.2811', 'naval.tourism@biliran.gov.ph', 'higatangan_sandbar.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(5, 5, 'Mainit Hot Spring', 'HOT_SPRING', 'Natural therapeutic mineral hot springs flowing down a geothermal river bed with temperature pools designed for relaxation and healing.', 40.00, '7:00 AM - 7:00 PM', '11.5511, 124.5422', 'caibiran.hotspring@biliran.gov.ph', 'mainit_spring.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(6, 7, 'Iyusan Rice Terraces', 'CULTURAL_HERITAGE', 'Spectacular centuries-old stone-walled rice terraces carved onto verdant mountain slopes reflecting community agricultural heritage.', 25.00, '6:00 AM - 5:30 PM', '11.5714, 124.4022', 'iyusan.brgy@almeria.gov.ph', 'iyusan_terraces.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(7, 2, 'Maripipi Pottery & Cultural Village', 'CULTURAL_HERITAGE', 'Traditional community hub where generational pottery artisans mold natural red clay pots (daba) and clay ovens using indigenous techniques.', 30.00, '8:00 AM - 4:00 PM', '11.7708, 124.3411', 'maripipi.artisan@biliran.gov.ph', 'maripipi_pottery.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00'),
(8, 6, 'Reco Falls', 'WATERFALL', 'A serene and tranquil swimming hole located upstream from Ulan-Ulan falls, offering calm emerald waters and natural diving ledges.', 30.00, '7:00 AM - 4:30 PM', '11.5894, 124.4219', 'almeria.tourism@biliran.gov.ph', 'reco_falls.jpg', 'ACTIVE', 1, '2026-01-10 10:00:00')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ----------------------------------------------------------------------------
-- 9. SEED TOURISM PACKAGES
-- ----------------------------------------------------------------------------
INSERT INTO tourism_packages (package_id, name, description, package_type, price_per_pax, duration_days, min_pax, max_pax, inclusions, exclusions, status, image_url, created_by) VALUES
(1, 'Sambawan Island Marine Paradise Escape', 'Experience Biliran’s premier marine sanctuary with roundtrip boat charter from Kawayan, hilltop viewpoint trek, snorkeling guide, and beach boodle feast.', 1850.00, 2, 4, 10, 'Boat charter, entrance fees, certified guide, camping tent/cottage, seafood lunch feast', 'Personal snacks, snorkeling gear rental', 'ACTIVE', 'package_sambawan.jpg', 1),
(2, 'Biliran Chasing Waterfalls Eco-Trek', 'A full-day thrilling adventure traversing Tinago Falls, Ulan-Ulan Falls, and Reco Falls with habal-habal transport and native lunch.', 1250.00, 1, 2, 8, 'Habal-habal transport, safety helmets, entrance fees to 3 falls, local trek guide, native lunch', 'Tips and personal souvenirs', 'ACTIVE', 'package_waterfalls.jpg', 1),
(3, 'Higatangan Shifting Sandbar & Heritage Cruise', 'Sail to Higatangan Island, walk the legendary shifting sandbar, and discover local weaving communities.', 1400.00, 1, 4, 8, 'Roundtrip chartered boat, environmental fee, seafood picnic, community guide', 'Alcoholic beverages', 'ACTIVE', 'package_higatangan.jpg', 1),
(4, 'Maripipi Artisan Culture & Pottery Immersion', 'Discover ancient pottery making in Bato, visit the historic watchtower, and sample authentic Maripipi delicacies.', 950.00, 1, 2, 6, 'Pottery clay workshop materials, local guide, afternoon native snack, village tour', 'Personal transport to Maripipi', 'ACTIVE', 'package_artisan.jpg', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ----------------------------------------------------------------------------
-- 10. SEED PACKAGE DESTINATIONS & SERVICES
-- ----------------------------------------------------------------------------
INSERT INTO package_destinations (package_id, destination_id, visit_order) VALUES
(1, 1, 1),
(2, 2, 1),
(2, 3, 2),
(2, 8, 3),
(3, 4, 1),
(4, 7, 1)
ON DUPLICATE KEY UPDATE visit_order = VALUES(visit_order);

INSERT INTO package_services (package_id, service_id) VALUES
(1, 1),
(1, 5),
(1, 7),
(2, 2),
(2, 3),
(3, 6),
(4, 9)
ON DUPLICATE KEY UPDATE service_id = VALUES(service_id);

-- ----------------------------------------------------------------------------
-- 11. SEED BOOKINGS
-- ----------------------------------------------------------------------------
INSERT INTO bookings (booking_id, booking_reference, tourist_id, package_id, booking_date, travel_date_start, travel_date_end, number_of_pax, total_amount, status, custom_notes) VALUES
(1, 'LL-2026-0001', 1, 1, '2026-02-15 10:00:00', '2026-03-20', '2026-03-21', 4, 7400.00, 'COMPLETED', 'Group of 4 photography enthusiasts, requesting sunset trek at Sambawan hill.'),
(2, 'LL-2026-0002', 1, 2, '2026-03-01 14:30:00', '2026-04-10', '2026-04-10', 2, 2500.00, 'CONFIRMED', 'Vegetarian lunch request for one guest.'),
(3, 'LL-2026-0003', 1, NULL, '2026-03-05 09:15:00', '2026-04-18', '2026-04-18', 3, 2100.00, 'PENDING', 'Custom booking: Habal-habal tour plus Pottery workshop.')
ON DUPLICATE KEY UPDATE total_amount = VALUES(total_amount);

-- ----------------------------------------------------------------------------
-- 12. SEED REVIEWS
-- ----------------------------------------------------------------------------
INSERT INTO reviews (review_id, booking_id, tourist_id, target_type, target_id, rating, comment, created_at) VALUES
(1, 1, 1, 'PACKAGE', 1, 5, 'Sambawan Island is sheer paradise! The boat ride was smooth, our guide Kuya Pedro took magnificent photos, and the seafood boodle fight was delicious!', '2026-03-22 08:30:00'),
(2, 1, 1, 'STAKEHOLDER', 1, 5, 'Kuya Pedro is very knowledgeable about the coral reefs and ensured all of us wore life jackets while snorkeling. Truly community-based excellence!', '2026-03-22 08:35:00'),
(3, 1, 1, 'DESTINATION', 1, 5, 'The view from the Sambawan hill at sunrise took our breath away. Clean facilities and well-maintained trail.', '2026-03-22 08:40:00')
ON DUPLICATE KEY UPDATE rating = VALUES(rating);

-- ----------------------------------------------------------------------------
-- 13. SEED ANNOUNCEMENTS (Tourism Information Management)
-- ----------------------------------------------------------------------------
INSERT INTO announcements (announcement_id, title, content, category, target_barangay_id, published_by, publish_date, expiry_date, status) VALUES
(1, 'Biliran Province 34th Foundation Anniversary & Tourism Expo', 'Join us from May 7 to 11 in Naval for the annual Province-Wide Tourism Expo, featuring local culinary showcases, artisanal handicrafts, and eco-tour discounts.', 'FESTIVAL', NULL, 2, '2026-04-01', '2026-05-15', 'PUBLISHED'),
(2, 'Sustainable Eco-Trekking Guidelines for Ulan-Ulan and Tinago Falls', 'All visitors are reminded to observe Leave No Trace principles: single-use plastics are prohibited within 50 meters of water basins. Certified community guides are mandatory.', 'TRAVEL_GUIDELINE', 6, 2, '2026-02-01', '2026-12-31', 'PUBLISHED'),
(3, 'Coast Guard Sea Travel Advisory: Calm Seas at Maripipi & Sambawan', 'Philippine Coast Guard reports favorable sea conditions across the Biliran Strait. Motorized tourist vessels from Kawayan and Naval are cleared for regular island crossings.', 'ANNOUNCEMENT', 1, 2, '2026-03-01', '2026-04-30', 'PUBLISHED')
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- ----------------------------------------------------------------------------
-- 14. SEED TOURISM ARRIVAL LOGS
-- ----------------------------------------------------------------------------
INSERT INTO tourism_arrival_logs (log_id, destination_id, log_date, local_tourists_count, domestic_tourists_count, foreign_tourists_count, total_revenue, recorded_by) VALUES
(1, 1, '2026-03-01', 45, 120, 18, 21960.00, 4),
(2, 1, '2026-03-02', 30, 85, 12, 15240.00, 4),
(3, 2, '2026-03-01', 60, 140, 5, 10250.00, 1),
(4, 3, '2026-03-01', 25, 90, 8, 4920.00, 1),
(5, 4, '2026-03-01', 20, 65, 4, 7120.00, 1)
ON DUPLICATE KEY UPDATE total_revenue = VALUES(total_revenue);

-- ----------------------------------------------------------------------------
-- 15. SEED AUDIT LOGS
-- ----------------------------------------------------------------------------
INSERT INTO audit_logs (log_id, user_id, username, action, entity_name, entity_id, ip_address, details, timestamp) VALUES
(1, 1, 'admin', 'SYSTEM_INITIALIZATION', 'DATABASE', 1, '127.0.0.1', 'Lakbay Lokal database initialized with seed data for Biliran Province.', '2026-01-01 00:00:00'),
(2, 4, 'brgy_olog', 'STAKEHOLDER_VERIFIED', 'STAKEHOLDER_PROFILE', 1, '127.0.0.1', 'Barangay Captain Ramon Corpin verified Sambawan Island Eco-Trek Guides.', '2026-01-15 09:30:00'),
(3, 10, 'tourist_ana', 'CREATE_BOOKING', 'BOOKINGS', 1, '127.0.0.1', 'Created booking reference LL-2026-0001 for Sambawan Island Paradise Escape.', '2026-02-15 10:00:00');
