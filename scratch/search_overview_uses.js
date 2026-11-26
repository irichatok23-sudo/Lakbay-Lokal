const fs = require('fs');
const appjs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const lines = appjs.split('\n');
lines.forEach((l, i) => {
    if (l.includes("'overview'") || l.includes('"overview"')) {
        console.log(`Line ${i+1}: ${l.trim()}`);
    }
});
