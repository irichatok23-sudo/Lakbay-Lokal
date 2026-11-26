-- ============================================================================
-- LAKBAY LOKAL: ANALYTICAL VIEWS AND REPORTING PROCEDURES
-- Target Location: Biliran Province, Philippines
-- ============================================================================

USE lakbay_lokal_db;

-- ----------------------------------------------------------------------------
-- 1. DESTINATION PERFORMANCE & FOOTFALL SUMMARY VIEW
-- ----------------------------------------------------------------------------
DROP VIEW IF EXISTS v_destination_footfall_summary;
CREATE VIEW v_destination_footfall_summary AS
SELECT 
    d.destination_id,
    d.name AS destination_name,
    d.category,
    b.name AS barangay_name,
    m.name AS municipality_name,
    d.entrance_fee,
    d.status,
    COALESCE(SUM(al.local_tourists_count), 0) AS total_local_visitors,
    COALESCE(SUM(al.domestic_tourists_count), 0) AS total_domestic_visitors,
    COALESCE(SUM(al.foreign_tourists_count), 0) AS total_foreign_visitors,
    COALESCE(SUM(al.local_tourists_count + al.domestic_tourists_count + al.foreign_tourists_count), 0) AS grand_total_visitors,
    COALESCE(SUM(al.total_revenue), 0) AS total_revenue_generated,
    COALESCE(ROUND(AVG(r.rating), 1), 5.0) AS average_rating,
    COUNT(DISTINCT r.review_id) AS total_reviews_count
FROM destinations d
JOIN barangays b ON d.barangay_id = b.barangay_id
JOIN municipalities m ON b.municipality_id = m.municipality_id
LEFT JOIN tourism_arrival_logs al ON d.destination_id = al.destination_id
LEFT JOIN reviews r ON r.target_type = 'DESTINATION' AND r.target_id = d.destination_id
GROUP BY d.destination_id, d.name, d.category, b.name, m.name, d.entrance_fee, d.status;

-- ----------------------------------------------------------------------------
-- 2. STAKEHOLDER PERFORMANCE & LIVELIHOOD SUMMARY VIEW
-- ----------------------------------------------------------------------------
DROP VIEW IF EXISTS v_stakeholder_performance_summary;
CREATE VIEW v_stakeholder_performance_summary AS
SELECT 
    sp.profile_id,
    sp.business_name,
    sp.category,
    u.full_name AS stakeholder_owner,
    b.name AS barangay_name,
    m.name AS municipality_name,
    sp.verification_status,
    COUNT(DISTINCT ts.service_id) AS active_services_count,
    COUNT(DISTINCT bsi.item_id) AS total_service_bookings,
    COALESCE(SUM(bsi.subtotal), 0.00) AS estimated_gross_income,
    COALESCE(ROUND(AVG(r.rating), 1), 5.0) AS average_rating,
    COUNT(DISTINCT r.review_id) AS review_count
FROM stakeholder_profiles sp
JOIN users u ON sp.user_id = u.user_id
JOIN barangays b ON sp.barangay_id = b.barangay_id
JOIN municipalities m ON b.municipality_id = m.municipality_id
LEFT JOIN tourism_services ts ON sp.profile_id = ts.stakeholder_id
LEFT JOIN booking_service_items bsi ON ts.service_id = bsi.service_id AND bsi.status IN ('ACCEPTED', 'COMPLETED')
LEFT JOIN reviews r ON r.target_type = 'STAKEHOLDER' AND r.target_id = sp.profile_id
GROUP BY sp.profile_id, sp.business_name, sp.category, u.full_name, b.name, m.name, sp.verification_status;

-- ----------------------------------------------------------------------------
-- 3. BARANGAY TOURISM OVERVIEW VIEW
-- ----------------------------------------------------------------------------
DROP VIEW IF EXISTS v_barangay_tourism_overview;
CREATE VIEW v_barangay_tourism_overview AS
SELECT 
    b.barangay_id,
    b.name AS barangay_name,
    m.name AS municipality_name,
    b.contact_person,
    b.contact_number,
    COUNT(DISTINCT d.destination_id) AS total_destinations,
    COUNT(DISTINCT sp.profile_id) AS total_stakeholders,
    COUNT(DISTINCT CASE WHEN sp.verification_status = 'VERIFIED' THEN sp.profile_id END) AS verified_stakeholders,
    COUNT(DISTINCT CASE WHEN sp.verification_status = 'PENDING' THEN sp.profile_id END) AS pending_stakeholders
FROM barangays b
JOIN municipalities m ON b.municipality_id = m.municipality_id
LEFT JOIN destinations d ON b.barangay_id = d.barangay_id
LEFT JOIN stakeholder_profiles sp ON b.barangay_id = sp.barangay_id
GROUP BY b.barangay_id, b.name, m.name, b.contact_person, b.contact_number;
