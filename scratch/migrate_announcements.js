const { execSync } = require('child_process');
const fs = require('fs');

async function migrate() {
    const mysqlBin = 'C:\\wamp64\\bin\\mysql\\mysql8.4.7\\bin\\mysql.exe';
    
    // Ensure migrations folder exists
    if (!fs.existsSync('migrations')) {
        fs.mkdirSync('migrations', { recursive: true });
    }

    const migrationSql = `
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
`;

    fs.writeFileSync('migrations/05_announcements_stage2.sql', migrationSql, 'utf8');
    fs.writeFileSync('sql/05_announcements_stage2.sql', migrationSql, 'utf8');

    console.log('Executing migration 05_announcements_stage2.sql...');
    execSync(`"${mysqlBin}" -u root lakbay_lokal_db < "migrations/05_announcements_stage2.sql"`, { stdio: 'inherit' });

    console.log('\n--- VERIFYING TABLE SCHEMA ---');
    const showCreate = execSync(`"${mysqlBin}" -u root lakbay_lokal_db -e "SHOW CREATE TABLE announcements\\G"`).toString();
    console.log(showCreate);

    console.log('\n--- VERIFYING EXISTING RECORDS REMAIN INTACT ---');
    const rows = execSync(`"${mysqlBin}" -u root lakbay_lokal_db -e "SELECT announcement_id, title, category, status, image_url, publish_date, created_at FROM announcements"`).toString();
    console.log(rows);
}

migrate().catch(e => console.error(e));
