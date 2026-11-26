const fs = require('fs');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');

const lines = css.split('\n');
lines.forEach((line, idx) => {
    if (line.includes('.glass-') || line.includes('#glass-') || line.includes('#login-portal-view') || line.includes('.portal-view')) {
        console.log(`L${idx+1}: ${line}`);
    }
});
