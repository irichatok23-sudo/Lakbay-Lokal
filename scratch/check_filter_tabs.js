const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('filter-tab') || l.includes('tab-btn') || l.includes('filter-btn') || l.includes('status-filter')) {
        console.log((i + 1) + ': ' + l.trim());
    }
});
