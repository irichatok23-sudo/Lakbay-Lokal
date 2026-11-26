const fs = require('fs');

// Search HTML for booking tables
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
console.log('=== HTML BOOKING TABLES ===');
html.forEach((line, idx) => {
    if (line.includes('booking') && (line.includes('<table') || line.includes('tbody') || line.includes('<thead>') || line.includes('table-'))) {
        console.log(`HTML:${idx + 1}: ${line.trim()}`);
    }
});

// Search JS for booking rendering
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('\n=== JS BOOKING FUNCTIONS ===');
js.forEach((line, idx) => {
    if (/function.*booking/i.test(line) || /render.*booking/i.test(line) || /load.*booking/i.test(line) || line.includes('bookings-table') || line.includes('booking-table')) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
