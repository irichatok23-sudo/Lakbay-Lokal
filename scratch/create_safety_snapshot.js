const fs = require('fs');
const path = require('path');

const targetDir = 'backups/pre_restoration_snapshot_20261123';
if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
}

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

console.log('Creating pre-restoration snapshot...');
copyDir('src', path.join(targetDir, 'src'));
fs.copyFileSync('build.ps1', path.join(targetDir, 'build.ps1'));
fs.copyFileSync('run.ps1', path.join(targetDir, 'run.ps1'));
if (fs.existsSync('package.json')) fs.copyFileSync('package.json', path.join(targetDir, 'package.json'));

console.log('Snapshot successfully created at:', targetDir);
