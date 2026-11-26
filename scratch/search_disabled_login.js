const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

appJs.split('\n').forEach((line, idx) => {
    if (/disabled|readOnly|pointer-events/i.test(line)) {
        if (/glass|login|auth/i.test(line)) {
            console.log(`L${idx+1}: ${line.trim()}`);
        }
    }
});
