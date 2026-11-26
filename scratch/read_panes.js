const fs = require('fs');
const content = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

function extractPane(id) {
    const start = content.indexOf(`id="${id}"`);
    if (start === -1) return 'NOT FOUND';
    // find next pane or end of pane
    const nextPane = content.indexOf('id="pane-', start + 10);
    const end = nextPane !== -1 ? nextPane : start + 3000;
    return content.substring(start, Math.min(start + 2500, end));
}

console.log('--- DESTINATIONS PANE ---');
console.log(extractPane('pane-destinations').substring(0, 1500));

console.log('--- STAKEHOLDERS PANE ---');
console.log(extractPane('pane-stakeholders').substring(0, 1500));
