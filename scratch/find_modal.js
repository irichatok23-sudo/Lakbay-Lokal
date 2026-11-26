const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('function openModal')) console.log((i + 1) + ': ' + l);
});
