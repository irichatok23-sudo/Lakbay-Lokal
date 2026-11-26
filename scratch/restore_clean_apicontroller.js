const fs = require('fs');
fs.copyFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'src/main/java/com/lakbaylokal/controller/ApiController.java');
console.log('Copied backup_phase2/ApiController_before_stage2_1.java.bak to ApiController.java');
