const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
const lines = content.split('\n');
const tokens = ['--ocean', '--teal', '--aqua', '--seafoam', '--white-cloud', '--coral'];

lines.forEach((line, idx) => {
    for (const t of tokens) {
        if (line.includes(t)) {
            console.log(`${idx + 1}: ${line.trim()}`);
            break;
        }
    }
});
