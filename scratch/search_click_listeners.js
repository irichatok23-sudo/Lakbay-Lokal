const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

appJs.split('\n').forEach((l, i) => {
    if (l.includes("addEventListener('click") || l.includes('addEventListener("click')) {
        console.log(`L${i+1}: ${l.trim()}`);
    }
});
