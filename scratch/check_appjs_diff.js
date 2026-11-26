const fs = require('fs');

console.log('app_before_stage2_1.js.bak size:', fs.statSync('backup_phase2/app_before_stage2_1.js.bak').size);
console.log('src/main/resources/web/js/app.js size:', fs.statSync('src/main/resources/web/js/app.js').size);

const bak = fs.readFileSync('backup_phase2/app_before_stage2_1.js.bak', 'utf8');
const src = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('bak includes isTourismOfficeOnly:', bak.includes('isTourismOfficeOnly'));
console.log('src includes isTourismOfficeOnly:', src.includes('isTourismOfficeOnly'));
console.log('bak includes switchAnnouncementStatusTab:', bak.includes('switchAnnouncementStatusTab'));
console.log('src includes switchAnnouncementStatusTab:', src.includes('switchAnnouncementStatusTab'));
