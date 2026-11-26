const fs = require('fs');
const testStage1 = fs.readFileSync('scratch/test_stage1_comprehensive.js', 'utf8');
console.log('testStage1 header (first 50 lines):');
console.log(testStage1.split('\n').slice(0, 50).join('\n'));
