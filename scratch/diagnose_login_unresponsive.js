const fs = require('fs');

const indexHtml = fs.readFileSync('src/main/resources/web/index.html', 'utf8');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== ALL MODAL ELEMENTS IN index.html ===');
const modalRegex = /id="(modal-[^"]+)"/g;
let m;
while ((m = modalRegex.exec(indexHtml)) !== null) {
    console.log(`- ${m[1]}`);
}

console.log('\n=== ALL ELEMENTS WITH z-index >= 100 IN style.css ===');
const css = fs.readFileSync('src/main/resources/web/css/style.css', 'utf8');
css.split('\n').forEach((line, idx) => {
    if (/z-index:\s*([0-9]{2,})/i.test(line)) {
        const match = line.match(/z-index:\s*([0-9]+)/i);
        if (match && parseInt(match[1]) >= 50) {
            console.log(`L${idx+1}: z-index=${match[1]} -> ${line.trim()}`);
        }
    }
});

console.log('\n=== LOGOUT FUNCTION IN app.js ===');
const logoutIdx = appJs.indexOf('function logout()');
console.log(appJs.substring(logoutIdx, logoutIdx + 1200));

console.log('\n=== showLoginView IN app.js ===');
const showLoginIdx = appJs.indexOf('function showLoginView()');
console.log(appJs.substring(showLoginIdx, showLoginIdx + 1200));
