const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = content.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('function switchTab(')) {
        console.log((idx+1) + ': ' + line);
    }
});
