
-- =============================================================================
-- Migration: Stage 2 Announcements Schema Enhancement
-- Idempotent non-destructive update
-- =============================================================================

-- 1. Ensure image_url column exists
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'lakbay_lokal_db' 
      AND TABLE_NAME = 'announcements' 
      AND COLUMN_NAME = 'image_url'
);

SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE announcements ADD COLUMN image_url VARCHAR(255) NULL AFTER target_barangay_id',
    'SELECT "image_url column already exists" AS message'
);
PREPARE exec_sql FROM @stmt;
EXECUTE exec_sql;
DEALLOCATE PREPARE exec_sql;

-- 2. Expand category ENUM to include DESTINATION_CLOSURE and ADVISORY
ALTER TABLE announcements 
MODIFY COLUMN category ENUM(
    'ANNOUNCEMENT',
    'EVENT',
    'FESTIVAL',
    'TRAVEL_GUIDELINE',
    'DESTINATION_CLOSURE',
    'ADVISORY'
) NOT NULL;

-- 3. Expand status ENUM to include DRAFT, PUBLISHED, ARCHIVED with default DRAFT
ALTER TABLE announcements 
MODIFY COLUMN status ENUM(
    'DRAFT',
    'PUBLISHED',
    'ARCHIVED'
) NOT NULL DEFAULT 'DRAFT';

-- 4. Allow publish_date to be nullable for drafts
ALTER TABLE announcements 
MODIFY COLUMN publish_date DATE NULL;
