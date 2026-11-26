const fs = require('fs');
const path = require('path');

function findBak(dir) {
    let results = [];
    fs.readdirSync(dir, { withFileTypes: true }).forEach(e => {
        const full = path.join(dir, e.name);
        if (e.isDirectory() && e.name !== '.git' && e.name !== 'node_modules') {
            results = results.concat(findBak(full));
        } else if (e.isFile() && (e.name.endsWith('.bak') || e.name.includes('stage1') || e.name.includes('pre_migration'))) {
            const stat = fs.statSync(full);
            results.push({ path: full, size: stat.size, mtime: stat.mtime });
        }
    });
    return results;
}

const baks = findBak('.');
baks.sort((a, b) => b.mtime - a.mtime);
baks.forEach(b => console.log(`${b.mtime.toISOString()}  ${b.size.toString().padStart(8)}  ${b.path}`));
