const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const regex = /localStorage\.[a-zA-Z]+/g;
let m;
const items = new Set();
while ((m = regex.exec(content)) !== null) {
    items.add(m[0]);
}
console.log('localStorage usages in app.js:', [...items]);

// Check if there are any mock or fallback destinations
const destMatches = [];
content.split('\n').forEach((l, i) => {
    if (l.includes('mock') || l.includes('seedDestinations') || l.includes('fallbackDestinations')) {
        destMatches.push(`${i+1}: ${l}`);
    }
});
console.log('Mock/fallback matches:', destMatches);
