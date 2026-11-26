const fs = require('fs');
const path = require('path');

const files = [
    'src/main/resources/web/index.html',
    ...fs.readdirSync('src/main/resources/web/js').map(f => 'src/main/resources/web/js/' + f)
];

files.forEach(file => {
    if (!fs.existsSync(file)) return;
    const lines = fs.readFileSync(file, 'utf8').split('\n');
    lines.forEach((line, idx) => {
        if (/banner/i.test(line)) {
            console.log(`${file}:${idx + 1}: ${line.trim()}`);
        }
    });
});
