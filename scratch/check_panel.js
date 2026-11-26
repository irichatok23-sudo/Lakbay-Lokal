const fs = require('fs');

const panel = fs.readFileSync('src/main/java/lakbaylokal/tourist/bookings/TouristBookingsPanel.java', 'utf8');
const lines = panel.split('\n');
lines.forEach((l, i) => {
    if (l.includes('Export') || l.includes('export')) {
        console.log(`Line ${i+1}: ${l}`);
    }
});
