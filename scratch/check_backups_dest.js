const fs = require('fs');

const bFiles = fs.readdirSync('backups').filter(f => f.endsWith('.sql'));
bFiles.forEach(f => {
    const c = fs.readFileSync('backups/' + f, 'utf8');
    const lines = c.split('\n');
    const destLines = lines.filter(l => l.includes('INSERT INTO `destinations`') || l.includes('INSERT INTO destinations'));
    console.log(`\n=== ${f} ===`);
    console.log(`Found ${destLines.length} insert statements`);
    destLines.forEach(l => console.log(l.substring(0, 300)));
});
