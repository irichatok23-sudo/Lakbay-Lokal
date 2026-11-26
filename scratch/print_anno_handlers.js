const fs = require('fs');
const lines = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8').split('\n');
for (let i = 1114; i < 1185; i++) {
    console.log((i+1) + ': ' + lines[i]);
}
