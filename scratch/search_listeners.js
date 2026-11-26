const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== GLOBAL EVENT LISTENERS IN app.js ===');
const listenerRegex = /(?:window|document|document\.body)\.addEventListener\(['"]([^'"]+)['"]/g;
let m;
while ((m = listenerRegex.exec(appJs)) !== null) {
    console.log(`Listener on ${m[0]}`);
}

console.log('\n=== KEYDOWN / KEYPRESS / INPUT LISTENERS ===');
appJs.split('\n').forEach((line, idx) => {
    if (/addEventListener\(['"](?:key|input|pointer|mouse|focus|blur)/i.test(line)) {
        console.log(`L${idx+1}: ${line.trim()}`);
    }
});
