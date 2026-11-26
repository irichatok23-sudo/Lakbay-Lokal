const fs = require('fs');
const current = fs.readFileSync('src/main/java/com/lakbaylokal/controller/ApiController.java', 'utf8');
const bak = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8');
console.log('Current length:', current.length, 'lines:', current.split('\n').length);
console.log('Bak length:', bak.length, 'lines:', bak.split('\n').length);
