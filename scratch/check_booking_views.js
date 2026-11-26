const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

// Find openViewBookingModal definition
const start = js.indexOf('function openViewBookingModal');
if (start !== -1) {
    console.log(js.substring(start, start + 3500));
}

// Find schedule table rendering
const schStart = js.indexOf('function loadStakeholderSchedule');
if (schStart !== -1) {
    console.log('\n--- loadStakeholderSchedule ---');
    console.log(js.substring(schStart, schStart + 2000));
}
