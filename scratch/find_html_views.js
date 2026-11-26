const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const lines = html.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('portal-view') || line.includes('id="app"') || line.includes('id="pane-') || line.includes('class="dashboard')) {
        console.log((idx+1) + ': ' + line.trim());
    }
});
