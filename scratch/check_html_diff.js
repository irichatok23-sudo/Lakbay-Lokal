const fs = require('fs');

console.log('index_before_stage2_1.html.bak size:', fs.statSync('backup_phase2/index_before_stage2_1.html.bak').size);
console.log('src/main/resources/web/index.html size:', fs.statSync('src/main/resources/web/index.html').size);
console.log('bin/web/index.html size:', fs.statSync('bin/web/index.html').size);

const bak = fs.readFileSync('backup_phase2/index_before_stage2_1.html.bak', 'utf8');
const src = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

console.log('bak includes overview-announcements-card:', bak.includes('overview-announcements-card'));
console.log('src includes overview-announcements-card:', src.includes('overview-announcements-card'));
console.log('bak includes anno-status-tabs:', bak.includes('anno-status-tabs'));
console.log('src includes anno-status-tabs:', src.includes('anno-status-tabs'));
