const fs = require('fs');

const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8').split('\n');
console.log('=== BADGES IN CSS ===');
css.forEach((line, idx) => {
    if (line.includes('.badge-')) {
        console.log(`CSS:${idx + 1}: ${line.trim()}`);
    }
});
