const fs = require('fs');
const appJs = fs.readFileSync('src/main/resources/web/js/app.js', 'utf8');

const idx = appJs.indexOf('function applyRolePermissions');
console.log(appJs.substring(idx, idx + 1500));
