const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

// Find ROLE_TABS or role tab permissions
const lines = content.split('\n');
lines.forEach((line, i) => {
    if (line.includes('ROLE_TABS') || line.includes('TOURIST:') || line.includes('STAKEHOLDER:') || line.includes('BARANGAY_OFFICIAL:')) {
        console.log(`${i+1}: ${line}`);
    }
});
