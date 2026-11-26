const fs = require('fs');

const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('=== DYNAMIC TABLES IN JS ===');
js.forEach((line, idx) => {
    if (line.includes('<table') || (line.includes('booking') && line.includes('<tr>'))) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
