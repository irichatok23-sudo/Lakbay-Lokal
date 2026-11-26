const fs = require('fs');

function search(dir) {
    fs.readdirSync(dir, { withFileTypes: true }).forEach(e => {
        const p = dir + '/' + e.name;
        if (e.isDirectory()) search(p);
        else if (e.name.endsWith('.java')) {
            const lines = fs.readFileSync(p, 'utf8').split('\n');
            lines.forEach((l, i) => {
                if (l.includes('ExportUtil.')) {
                    console.log(`${p}:${i+1}: ${l.trim()}`);
                }
            });
        }
    });
}
search('src/main/java');
