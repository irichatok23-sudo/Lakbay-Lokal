const fs = require('fs');
const lines = fs.readFileSync('backup_phase2/ApiController_before_stage2_1.java.bak', 'utf8').split('\n');

lines.forEach((l, i) => {
    if (l.includes('handleGetAnnouncements') || l.includes('handleSaveAnnouncement') || l.includes('handleDeleteAnnouncement')) {
        console.log((i+1) + ': ' + l.trim());
    }
});
