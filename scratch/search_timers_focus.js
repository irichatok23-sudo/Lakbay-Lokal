const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

console.log('=== setInterval in app.js ===');
appJs.split('\n').forEach((l, i) => {
    if (l.includes('setInterval')) {
        console.log(`L${i+1}: ${l.trim()}`);
    }
});

console.log('\n=== .focus() in app.js ===');
appJs.split('\n').forEach((l, i) => {
    if (l.includes('.focus(')) {
        console.log(`L${i+1}: ${l.trim()}`);
    }
});
