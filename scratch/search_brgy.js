const fs = require('fs');

const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('=== BARANGAY RECORDS TABLE ===');
js.forEach((line, idx) => {
    if (line.includes('loadBarangayRecordsTable') || line.includes('barangay-full-body')) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
