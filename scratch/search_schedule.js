const fs = require('fs');

const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('=== STAKEHOLDER SCHEDULE FUNCTION ===');
js.forEach((line, idx) => {
    if (line.includes('function loadStakeholderSchedule') || line.includes('schedule-table-body')) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
