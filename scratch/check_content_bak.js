const fs = require('fs');
const content = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8');
console.log('has isTourismOfficeOnly:', content.includes('isTourismOfficeOnly'));
console.log('has /api/announcements:', content.includes('/api/announcements'));
