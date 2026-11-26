const fs = require('fs');

console.log('Restoring pre-Stage-2 files...');

// 1. ApiController.java
fs.copyFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'src/main/java/com/lakbaylokal/controller/ApiController.java');
console.log('[OK] Restored src/main/java/com/lakbaylokal/controller/ApiController.java');

// 2. AnnouncementDAO.java
fs.copyFileSync('backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/dao/AnnouncementDAO.java', 'src/main/java/com/lakbaylokal/dao/AnnouncementDAO.java');
console.log('[OK] Restored src/main/java/com/lakbaylokal/dao/AnnouncementDAO.java');

// 3. AnnouncementDAOImpl.java
fs.copyFileSync('backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/dao/impl/AnnouncementDAOImpl.java', 'src/main/java/com/lakbaylokal/dao/impl/AnnouncementDAOImpl.java');
console.log('[OK] Restored src/main/java/com/lakbaylokal/dao/impl/AnnouncementDAOImpl.java');

// 4. AnnouncementService.java
fs.copyFileSync('backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/service/AnnouncementService.java', 'src/main/java/com/lakbaylokal/service/AnnouncementService.java');
console.log('[OK] Restored src/main/java/com/lakbaylokal/service/AnnouncementService.java');

// 5. Announcement.java (both packages)
fs.copyFileSync('backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/model/Announcement.java', 'src/main/java/com/lakbaylokal/model/Announcement.java');
fs.copyFileSync('backups/stakeholder_fix_backup/src/main/java/lakbaylokal/model/Announcement.java', 'src/main/java/lakbaylokal/model/Announcement.java');
console.log('[OK] Restored Announcement.java models');

// 6. DatabaseInitializer.java
fs.copyFileSync('backups/stakeholder_fix_backup/src/main/java/com/lakbaylokal/database/DatabaseInitializer.java', 'src/main/java/com/lakbaylokal/database/DatabaseInitializer.java');
console.log('[OK] Restored DatabaseInitializer.java');

console.log('\nRestoration of Java files complete.');
