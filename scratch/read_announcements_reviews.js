const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

function showSection(id) {
    const s = content.indexOf(`id="${id}"`);
    if (s === -1) return `${id} not found`;
    return content.substring(s, s + 1500);
}

console.log('--- ANNOUNCEMENTS ---');
console.log(showSection('pane-announcements'));

console.log('--- REVIEWS ---');
console.log(showSection('pane-reviews'));
