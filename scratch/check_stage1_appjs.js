const fs = require('fs');

const bak = fs.readFileSync('backup_phase2/app_before_stage2_1.js.bak', 'utf8');
const cur = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('bak includes resubmitStakeholderApplication:', bak.includes('resubmitStakeholderApplication'));
console.log('cur includes resubmitStakeholderApplication:', cur.includes('resubmitStakeholderApplication'));

console.log('bak includes openResubmitModal:', bak.includes('openResubmitModal'));
console.log('cur includes openResubmitModal:', cur.includes('openResubmitModal'));

console.log('bak includes loadStakeholderDashboardOverview:', bak.includes('loadStakeholderDashboardOverview'));
console.log('cur includes loadStakeholderDashboardOverview:', cur.includes('loadStakeholderDashboardOverview'));
