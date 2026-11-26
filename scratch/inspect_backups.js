const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('=== Directories in root ===');
fs.readdirSync('.').forEach(item => {
    try {
        const stat = fs.statSync(item);
        if (stat.isDirectory()) {
            console.log(`[DIR]  ${item.padEnd(30)} ${stat.mtime.toISOString()}`);
        }
    } catch (e) {}
});

console.log('\n=== backups/ folder contents ===');
if (fs.existsSync('backups')) {
    fs.readdirSync('backups').forEach(item => {
        const full = path.join('backups', item);
        const stat = fs.statSync(full);
        console.log(`${stat.isDirectory() ? '[DIR] ' : '[FILE]'} ${item.padEnd(35)} ${stat.mtime.toISOString()} (${stat.size} bytes)`);
    });
}

console.log('\n=== backup_phase2/ folder contents ===');
if (fs.existsSync('backup_phase2')) {
    fs.readdirSync('backup_phase2').forEach(item => {
        const full = path.join('backup_phase2', item);
        const stat = fs.statSync(full);
        console.log(`${stat.isDirectory() ? '[DIR] ' : '[FILE]'} ${item.padEnd(35)} ${stat.mtime.toISOString()} (${stat.size} bytes)`);
    });
}

console.log('\n=== Git Log (last 10 commits) ===');
try {
    const gitLog = execSync('git log -n 10 --oneline', { encoding: 'utf8' });
    console.log(gitLog);
} catch (e) {
    console.log('Git log failed:', e.message);
}
