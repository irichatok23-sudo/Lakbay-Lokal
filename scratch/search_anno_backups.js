const fs = require('fs');
const path = require('path');

function searchBackups(fileName) {
    const list = [];
    function scan(d) {
        fs.readdirSync(d, { withFileTypes: true }).forEach(e => {
            const full = path.join(d, e.name);
            if (e.isDirectory() && e.name !== '.git' && e.name !== 'node_modules' && !full.includes('pre_restoration_snapshot')) {
                scan(full);
            } else if (e.isFile() && e.name.toLowerCase() === fileName.toLowerCase()) {
                list.push({ path: full, size: fs.statSync(full).size, mtime: fs.statSync(full).mtime });
            }
        });
    }
    scan('backups');
    scan('backup_phase2');
    return list;
}

['AnnouncementDAO.java', 'AnnouncementDAOImpl.java', 'AnnouncementService.java', 'Announcement.java', 'DatabaseInitializer.java'].forEach(f => {
    console.log(`\n=== Backups for ${f} ===`);
    searchBackups(f).forEach(b => console.log(`${b.mtime.toISOString()}  ${b.size.toString().padStart(6)}  ${b.path}`));
});
