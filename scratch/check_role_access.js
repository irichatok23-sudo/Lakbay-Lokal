const fs = require('fs');

const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');
const lines = appJs.split('\n');
const start = lines.findIndex(l => l.includes('function canRoleAccessTab'));
console.log(lines.slice(start, start + 45).join('\n'));
