const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
lines.forEach((l, i) => {
    if (l.includes('pane-announcements') || l.includes('modal-add-announcement') || l.includes('announcement-modal') || l.includes('announcements-grid')) {
        console.log((i + 1) + ': ' + l.trim());
    }
});
