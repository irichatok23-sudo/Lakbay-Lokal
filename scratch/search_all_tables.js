const fs = require('fs');

const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
console.log('=== ALL TABLES IN HTML ===');
html.forEach((line, idx) => {
    if (line.includes('<table') || line.includes('id="table-') || line.includes('tbody id=')) {
        console.log(`HTML:${idx + 1}: ${line.trim()}`);
    }
});
