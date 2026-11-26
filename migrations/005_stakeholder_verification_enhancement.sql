-- ==============================================================================
-- LAKBAY LOKAL — MIGRATION 005
-- STAKEHOLDER VERIFICATION LIFECYCLE & AUDIT TRAIL ALIGNMENT
-- Target Database: lakbay_lokal_db
-- Safety: Non-destructive, idempotent, preserves all existing records
-- ==============================================================================

USE `lakbay_lokal_db`;

-- 1. Align verification_records.status ENUM with complete lifecycle values
-- Preserves existing 'APPROVED' and 'REQUEST_CORRECTION' while adding 'VERIFIED', 'NEEDS_CORRECTION', and 'UNDER_REVIEW'
ALTER TABLE `verification_records` 
MODIFY COLUMN `status` ENUM('PENDING','UNDER_REVIEW','NEEDS_CORRECTION','VERIFIED','REJECTED','APPROVED','REQUEST_CORRECTION') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING';

-- 2. Add previous_status column to track status transitions in verification history
-- Idempotent execution using dynamic SQL check against information_schema
SET @col_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'verification_records' 
      AND COLUMN_NAME = 'previous_status'
);

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `verification_records` ADD COLUMN `previous_status` VARCHAR(50) NULL DEFAULT NULL AFTER `reviewer_id`', 
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
