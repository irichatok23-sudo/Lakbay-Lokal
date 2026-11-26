const fs = require('fs');
const html = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const lines = html.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('class="modal') || line.includes('class="glass-error') || line.includes('id="modal-')) {
        console.log((idx+1) + ': ' + line.trim());
    }
});
