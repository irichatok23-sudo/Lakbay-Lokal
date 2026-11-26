const fs = require('fs');

const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== User Profile triggers in index.html ===');
indexHtml.split('\n').forEach((l, i) => {
    if (/openUserProfileModal|profile/i.test(l)) {
        console.log(`L${i+1}: ${l.trim()}`);
    }
});

console.log('=== openUserProfileModal in app.js ===');
const pIdx = appJs.indexOf('function openUserProfileModal');
if (pIdx !== -1) {
    console.log(appJs.substring(pIdx, pIdx + 1200));
}
