const fs = require('fs');
const js = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = js.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('function showToast(')) {
        console.log((idx+1) + ': ' + line.trim());
    }
});
