const fs = require('fs');

const bak = fs.readFileSync('backup_phase2/index_before_stage2_1.html.bak', 'utf8');
const cur = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

console.log('bak includes stakeholder-overview-dashboard:', bak.includes('stakeholder-overview-dashboard'));
console.log('cur includes stakeholder-overview-dashboard:', cur.includes('stakeholder-overview-dashboard'));

console.log('bak includes modal-stakeholder-resubmit:', bak.includes('modal-stakeholder-resubmit'));
console.log('cur includes modal-stakeholder-resubmit:', cur.includes('modal-stakeholder-resubmit'));

console.log('bak includes overview-verification-warning:', bak.includes('overview-verification-warning'));
console.log('cur includes overview-verification-warning:', cur.includes('overview-verification-warning'));
