const fs = require('fs');
const h = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const a = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('index.html:');
h.split('\n').forEach((l, i) => {
    if (l.includes('openLoginModal')) console.log(`${i+1}: ${l.trim()}`);
});

console.log('app.js:');
a.split('\n').forEach((l, i) => {
    if (l.includes('openLoginModal')) console.log(`${i+1}: ${l.trim()}`);
});
