const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');

for (let i = 0; i < js.length; i++) {
    if (js[i].includes('function renderBookingsList') || js[i].includes('function updateBookingStatus') || js[i].includes('function updateBookingItemStatus')) {
        console.log(`--- Line ${i+1} ---`);
        for (let j = i; j < Math.min(js.length, i + 60); j++) {
            console.log((j+1) + ': ' + js[j]);
        }
    }
}
