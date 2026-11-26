const fs = require('fs');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');

const lines = css.split('\n');
lines.forEach((l, idx) => {
    if (l.includes('position: fixed') || l.includes('position: sticky')) {
        console.log(`L${idx+1}: ${l.trim()} | Context: ${lines[Math.max(0, idx-2)].trim()} -> ${lines[Math.max(0, idx-1)].trim()}`);
    }
});
