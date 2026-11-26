const fs = require('fs');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
const lines = css.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('background') && (line.includes('#fff') || line.includes('white') || line.includes('body') || line.includes('portal'))) {
        console.log((idx+1) + ': ' + line.trim());
    }
});
