const fs = require('fs');

const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = appJs.split('\n');
const start = lines.findIndex(l => l.includes('ROLE_PERMITTED_TABS'));
console.log(lines.slice(start, start + 30).join('\n'));
