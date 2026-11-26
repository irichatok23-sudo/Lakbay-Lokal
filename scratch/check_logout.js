const fs = require('fs');
const appjs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const idx = appjs.indexOf('function logout()');
console.log(appjs.substring(idx, idx + 500));
