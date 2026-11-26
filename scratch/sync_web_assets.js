const fs = require('fs');
const path = require('path');

function copyDir(src, dest) {
    if (!fs.existsSync(src)) return;
    if (!fs.existsSync(dest)) fs.mkdirSync(dest, { recursive: true });
    fs.readdirSync(src, { withFileTypes: true }).forEach(e => {
        const srcPath = path.join(src, e.name);
        const destPath = path.join(dest, e.name);
        if (e.isDirectory()) {
            copyDir(srcPath, destPath);
        } else {
            fs.copyFileSync(srcPath, destPath);
        }
    });
}

copyDir('src/main/resources/web', 'bin/web');
console.log('Synchronized web assets from src/main/resources/web to bin/web');
