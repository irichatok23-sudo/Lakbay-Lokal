const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');

function printSection(secId) {
    let printing = false;
    for (let i = 0; i < lines.length; i++) {
        if (lines[i].includes('id="' + secId + '"')) printing = true;
        if (printing) {
            console.log((i + 1) + ': ' + lines[i]);
            if (lines[i].includes('</section>')) break;
        }
    }
}

console.log('--- pane-admin ---');
printSection('pane-admin');
console.log('--- pane-lgu-monitoring ---');
printSection('pane-lgu-monitoring');
console.log('--- pane-barangay-records ---');
printSection('pane-barangay-records');
