const mysql = require('mysql2/promise');

async function checkDatabase() {
    try {
        const conn = await mysql.createConnection({
            host: '127.0.0.1',
            port: 3306,
            user: 'root',
            password: '',
            database: 'lakbay_lokal_db'
        });

        const tables = ['users', 'destinations', 'stakeholder_profiles', 'bookings', 'announcements', 'verification_records', 'audit_logs'];
        console.log('=== Live Database Record Counts in lakbay_lokal_db ===');
        for (const t of tables) {
            try {
                const [rows] = await conn.query(`SELECT COUNT(*) as count FROM ${t}`);
                console.log(`  ${t.padEnd(25)} : ${rows[0].count} records`);
            } catch (err) {
                console.log(`  ${t.padEnd(25)} : error (${err.message})`);
            }
        }
        await conn.end();
    } catch (e) {
        console.error('MySQL connection error:', e.message);
    }
}

checkDatabase();
