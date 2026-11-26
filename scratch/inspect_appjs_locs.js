const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8').split('\n');

console.log('Total lines in app.js:', lines.length);

function findMatches(pattern) {
    const res = [];
    lines.forEach((l, i) => {
        if (l.includes(pattern)) res.push((i + 1) + ': ' + l.trim());
    });
    return res;
}

console.log('btnAddAnnounce:', findMatches('btnAddAnnounce'));
console.log('loadAnnouncementsList:', findMatches('async function loadAnnouncementsList'));
console.log('renderAnnouncementsList:', findMatches('function renderAnnouncementsList'));
console.log('openAddAnnouncementModal:', findMatches('function openAddAnnouncementModal'));
console.log('handleAddAnnouncementSubmit:', findMatches('async function handleAddAnnouncementSubmit'));
