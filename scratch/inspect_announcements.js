const { execSync } = require('child_process');
const fs = require('fs');
const http = require('http');

async function main() {
    // 1. Check mysqldump location
    const dumpBin = 'C:\\wamp64\\bin\\mysql\\mysql8.4.7\\bin\\mysqldump.exe';
    const dumpExists = fs.existsSync(dumpBin);
    console.log('mysqldump exists:', dumpExists);

    if (!fs.existsSync('backups')) {
        fs.mkdirSync('backups', { recursive: true });
    }
    const backupFile = 'backups/backup_stage2_pre_migration_20261123.sql';

    if (dumpExists) {
        console.log('Creating backup via mysqldump...');
        execSync(`"${dumpBin}" -u root --databases lakbay_lokal_db > "${backupFile}"`, { stdio: 'inherit' });
        const stat = fs.statSync(backupFile);
        console.log(`Backup created successfully: ${backupFile} (${stat.size} bytes)`);
    }

    // 2. Query MySQL using mysql CLI or Java to inspect table schema
    const mysqlBin = 'C:\\wamp64\\bin\\mysql\\mysql8.4.7\\bin\\mysql.exe';
    if (fs.existsSync(mysqlBin)) {
        console.log('\n--- SHOW CREATE TABLE announcements ---');
        const showCreate = execSync(`"${mysqlBin}" -u root lakbay_lokal_db -e "SHOW CREATE TABLE announcements\\G"`).toString();
        console.log(showCreate);

        console.log('\n--- SELECT * FROM announcements ---');
        const rows = execSync(`"${mysqlBin}" -u root lakbay_lokal_db -e "SELECT announcement_id, title, category, status, published_by, created_at FROM announcements"`).toString();
        console.log(rows);

        console.log('\n--- SELECT DISTINCT category, status FROM announcements ---');
        const distinct = execSync(`"${mysqlBin}" -u root lakbay_lokal_db -e "SELECT DISTINCT category FROM announcements; SELECT DISTINCT status FROM announcements;"`).toString();
        console.log(distinct);
    }
}

main().catch(err => console.error('Error:', err));
