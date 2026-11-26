const fs = require('fs');

const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');

console.log('=== LOGOUT in app.js ===');
appJs.split('\n').forEach((line, idx) => {
    if (/logout/i.test(line)) {
        console.log(`L${idx+1}: ${line}`);
    }
});

console.log('\n=== LOGOUT in index.html ===');
indexHtml.split('\n').forEach((line, idx) => {
    if (/logout/i.test(line)) {
        console.log(`L${idx+1}: ${line}`);
    }
});

console.log('\n=== LOGIN in app.js ===');
appJs.split('\n').forEach((line, idx) => {
    if (/function (?:handleLogin|login|openLoginModal|initLogin)/i.test(line)) {
        console.log(`L${idx+1}: ${line}`);
    }
});
