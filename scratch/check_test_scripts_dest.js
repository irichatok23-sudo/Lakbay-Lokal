const fs = require('fs');

const p1 = fs.readFileSync('scripts/test_phase1.ps1', 'utf8');
const p2 = fs.readFileSync('scripts/test_backend_and_apis.ps1', 'utf8');

console.log('test_phase1 destinations:', p1.split('\n').filter(l => l.includes('/api/destinations')).length);
console.log('test_backend_and_apis destinations:', p2.split('\n').filter(l => l.includes('/api/destinations')).length);

// Print any create destination in test scripts
p1.split('\n').forEach((l, i) => {
    if (l.includes('POST') && l.includes('destination')) console.log(`phase1 line ${i+1}: ${l}`);
});
p2.split('\n').forEach((l, i) => {
    if (l.includes('POST') && l.includes('destination')) console.log(`backend line ${i+1}: ${l}`);
});
