const fs = require('fs');

const baks = [
    'backup_phase2/ApiController_before_stage2_1.java.bak',
    'backup_phase2/ApiController_stage1_resubmit.java.bak',
    'backup_phase2/ApiController_stage1.java.bak',
    'backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/controller/ApiController.java',
    'backups/role_visibility_fix/ApiController.java',
    'backups/rbac_master_backup_20261120_094946/src/main/java/com/lakbaylokal/controller/ApiController.java'
];

baks.forEach(p => {
    if (fs.existsSync(p)) {
        const stat = fs.statSync(p);
        const content = fs.readFileSync(p, 'utf8');
        console.log(`\n--- ${p} (${stat.size} bytes, ${stat.mtime.toISOString()}) ---`);
        console.log('has /api/stakeholders/resubmit:', content.includes('/api/stakeholders/resubmit'));
        console.log('has /api/verifications:', content.includes('/api/verifications'));
        console.log('has /api/announcements/stats:', content.includes('/api/announcements/stats'));
        console.log('has isTourismOfficeOnly:', content.includes('isTourismOfficeOnly'));
        console.log('has isTourismOffice:', content.includes('isTourismOffice'));
        console.log('has isLguOrHigher:', content.includes('isLguOrHigher'));
    }
});
