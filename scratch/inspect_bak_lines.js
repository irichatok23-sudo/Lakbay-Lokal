const fs = require('fs');
const bak = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8');
const lines = bak.split('\n');
console.log('Total lines:', lines.length);
lines.forEach((l, i) => {
    if (l.includes('isTourismOffice') || l.includes('handleAnnouncement') || l.includes('/api/announcements')) {
        console.log((i + 1) + ': ' + l.trim());
    }
});
