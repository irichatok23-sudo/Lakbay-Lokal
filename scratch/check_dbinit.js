const fs = require('fs');

const bak = fs.readFileSync('backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/database/DatabaseInitializer.java', 'utf8');
const cur = fs.readFileSync('src/main/java/com/lakbaylokal/database/DatabaseInitializer.java', 'utf8');

console.log('bak has ensureVerificationStatuses:', bak.includes('ensureVerificationStatuses'));
console.log('cur has ensureVerificationStatuses:', cur.includes('ensureVerificationStatuses'));

console.log('bak has ensureAnnouncementSchema:', bak.includes('ensureAnnouncementSchema'));
console.log('cur has ensureAnnouncementSchema:', cur.includes('ensureAnnouncementSchema'));
