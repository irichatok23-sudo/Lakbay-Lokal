const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
const lines = content.split('\n');
const searchStrings = [
    '123B55', '087F8C', '35BFC0', 'DDF4EF', 'F5FAFC', 'F28C70',
    '18, 59, 85', '8, 127, 140', '53, 191, 192', '221, 244, 239', '245, 250, 252'
];

lines.forEach((line, idx) => {
    for (const s of searchStrings) {
        if (line.toUpperCase().includes(s.toUpperCase())) {
            console.log(`${idx + 1}: ${line.trim()}`);
            break;
        }
    }
});
