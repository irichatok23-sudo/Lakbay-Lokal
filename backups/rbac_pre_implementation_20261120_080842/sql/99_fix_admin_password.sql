-- ============================================================================
-- LAKBAY LOKAL: EMERGENCY FIX — Repair admin password hash
-- Run this script once against lakbay_lokal_db to fix corrupted admin hash.
-- Password for all pre-seeded accounts: password123
-- Salt: lakbaysalt2026
-- Correct SHA-256(password123 + lakbaysalt2026) =
-- 8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6
-- ============================================================================

USE lakbay_lokal_db;

-- Fix admin password hash (was stored as literal 'admin123\r\n' - broken seed data)
UPDATE users
SET password_hash = '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6',
    salt = 'lakbaysalt2026'
WHERE username = 'admin'
  AND password_hash NOT LIKE 'pbkdf2%'
  AND password_hash != '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6';

-- Verify all seed accounts have the correct hash (insurance)
UPDATE users
SET password_hash = '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6'
WHERE salt = 'lakbaysalt2026'
  AND password_hash NOT LIKE 'pbkdf2%'
  AND password_hash != '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6'
  AND status = 'ACTIVE';

SELECT username, role_id, status,
       CASE WHEN password_hash = '8c16d0213fba89b11e704e1dc1dbf539343f256ea837ce61ac78d6533c3857c6' THEN 'SHA256_CORRECT'
            WHEN password_hash LIKE 'pbkdf2%' THEN 'PBKDF2_OK'
            ELSE CONCAT('BROKEN:', LEFT(password_hash,20))
       END AS hash_status
FROM users
ORDER BY role_id;
