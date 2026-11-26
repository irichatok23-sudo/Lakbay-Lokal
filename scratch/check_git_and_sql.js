const fs = require('fs');
const { execSync } = require('child_process');

try {
    const gitLog = execSync('git log -n 5 --oneline', { encoding: 'utf8' });
    console.log('--- GIT LOG ---');
    console.log(gitLog);
} catch (e) {
    console.log('Git log failed:', e.message);
}

// Check sql files
const sqlFiles = fs.readdirSync('sql');
console.log('\n--- SQL FILES ---');
sqlFiles.forEach(f => {
    const c = fs.readFileSync('sql/' + f, 'utf8');
    const destInserts = c.split('\n').filter(l => l.includes('INSERT INTO destinations') || l.includes('destinations ('));
    console.log(`${f}: ${destInserts.length} destination inserts`);
});

// Check backups folder
if (fs.existsSync('backups')) {
    const bFiles = fs.readdirSync('backups');
    console.log('\n--- BACKUPS FOLDER ---');
    bFiles.forEach(f => console.log(f));
}
