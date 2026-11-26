const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('renderTouristDashboard') || l.includes('loadTourist') || l.includes('renderAdminDashboard') || l.includes('renderLgu') || l.includes('loadLgu') || l.includes('renderBarangay') || l.includes('loadBarangay') || l.includes('renderStakeholder') || l.includes('loadStakeholder')) {
        console.log((i + 1) + ': ' + l.trim());
    }
});
