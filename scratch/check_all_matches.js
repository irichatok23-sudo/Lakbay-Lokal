const fs = require('fs');
const h = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const lines = h.split('\n');
lines.forEach((l, i) => {
    if (l.match(/(export|download|file-csv|file-pdf|printreport)/i)) {
        console.log(`Line ${i+1}: ${l.trim()}`);
    }
});
