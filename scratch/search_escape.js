const fs = require('fs');

const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
console.log('=== escapeHtml ===');
js.forEach((line, idx) => {
    if (line.includes('function escapeHtml')) {
        console.log(`JS:${idx + 1}: ${line.trim()}`);
    }
});
