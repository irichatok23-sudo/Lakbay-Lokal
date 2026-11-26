const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('announcement')) {
        console.log((i + 1) + ': ' + l.trim());
    }
});
