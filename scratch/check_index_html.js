const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const lines = content.split('\n');
const patterns = ['123B55', '087F8C', '35BFC0', 'DDF4EF', 'F5FAFC', '#032219', '#064e3b', '#059669'];

lines.forEach((l, i) => {
    for (const p of patterns) {
        if (l.toUpperCase().includes(p.toUpperCase())) {
            console.log(`${i+1}: ${l.trim()}`);
            break;
        }
    }
});
