const fs = require('fs');

const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('=== bookingReference IN JS ===');
js.forEach((line, idx) => {
    if (line.includes('bookingReference') || line.includes('b.bookingReference')) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
