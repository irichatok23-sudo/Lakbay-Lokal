const mysql = require('mysql2');
const conn = mysql.createConnection({
    host: '127.0.0.1',
    user: 'root',
    password: '',
    database: 'lakbay_lokal_db'
});

async function run() {
    conn.query("SHOW TABLES", (err, tables) => {
        if (err) {
            console.error('Connection error:', err);
            conn.end();
            return;
        }
        console.log('Tables:', tables);
        conn.query("DESCRIBE destinations", (err, cols) => {
            console.log('destinations barangay_id:', cols.filter(c => c.Field === 'barangay_id'));
            conn.query("DESCRIBE stakeholder_profiles", (err, cols2) => {
                console.log('stakeholder_profiles barangay_id:', cols2.filter(c => c.Field === 'barangay_id'));
                conn.query("DESCRIBE verification_records", (err, cols3) => {
                    console.log('verification_records barangay_id:', cols3.filter(c => c.Field === 'barangay_id'));
                    conn.query("DESCRIBE users", (err, cols4) => {
                        console.log('users barangay_id:', cols4.filter(c => c.Field === 'barangay_id'));
                        conn.end();
                    });
                });
            });
        });
    });
}
run();
