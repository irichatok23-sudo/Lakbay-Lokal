const fs = require('fs');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
const lines = css.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('glass-input') || line.includes('glass-field') || line.includes('glass-btn-toggle-pwd') || line.includes('glass-portal-card')) {
        console.log((idx+1) + ': ' + line);
    }
});
