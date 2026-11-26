const fs = require('fs');
const lines = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8').split('\n');

console.log('--- Lines 70-90 ---');
for (let i = 69; i < 90; i++) console.log((i+1) + ': ' + lines[i]);

console.log('--- Lines 235-255 ---');
for (let i = 234; i < 255; i++) console.log((i+1) + ': ' + lines[i]);
