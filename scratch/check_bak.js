const fs = require('fs');
console.log('size of before_stage2_1:', fs.statSync('backup_phase2/ApiController_before_stage2_1.java.bak').size);
console.log('size of stage1:', fs.statSync('backup_phase2/ApiController_stage1.java.bak').size);
const sample = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8').substring(0, 1000);
console.log('sample:\n', sample);
