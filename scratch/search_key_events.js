const fs = require('fs');
const path = require('path');

const files = [
    'src/main/resources/web/index.html',
    'src/main/resources/web/js/app.js',
    'src/main/resources/web/js/pdf-export.js',
    'src/main/resources/web/css/style.css',
    'electron/main.js',
    'electron/preload.js'
];

files.forEach(f => {
    if (!fs.existsSync(f)) return;
    const content = fs.readFileSync(f, 'utf8');
    content.split('\n').forEach((line, idx) => {
        if (/keydown|keyup|keypress|focus|blur|trap|webview|overlay|modal/i.test(line)) {
            if (/addEvent|onkey|onfocus|onblur|window|document|tabindex|autofocus/i.test(line)) {
                console.log(`[${f}:${idx+1}] ${line.trim()}`);
            }
        }
    });
});
