const fs = require('fs');
function searchFile(dir, name) {
    if (!fs.existsSync(dir)) return [];
    let res = [];
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    for (const e of entries) {
        const full = dir + '/' + e.name;
        if (e.isDirectory() && e.name !== '.git' && e.name !== 'node_modules') {
            res = res.concat(searchFile(full, name));
        } else if (e.isFile() && e.name.toLowerCase().includes(name.toLowerCase())) {
            res.push(full);
        }
    }
    return res;
}
console.log('ApiController backups:', searchFile('.', 'ApiController'));
