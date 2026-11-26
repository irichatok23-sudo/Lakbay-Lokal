const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

appJs.split('\n').forEach((line, idx) => {
    if (line.includes('isSubmittingLogin')) {
        console.log(`L${idx+1}: ${line.trim()}`);
    }
});
