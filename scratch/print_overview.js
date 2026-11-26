const fs = require('fs');
const lines = fs.readFileSync('src/main/resources/web/index.html', 'utf8').split('\n');
let inOverview = false;
for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('id="pane-overview"')) {
        inOverview = true;
    }
    if (inOverview) {
        console.log((i + 1) + ': ' + lines[i]);
        if (lines[i].includes('</section>')) {
            break;
        }
    }
}
