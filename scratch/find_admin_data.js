const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('function loadAdminData') || l.includes('function renderOverviewTopDestinations')) {
        console.log((i + 1) + ': ' + l.trim());
    }
});
